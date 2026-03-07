package com.annabenson.viand.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.BuildConfig;
import com.annabenson.viand.adapters.ChatAdapter;
import com.annabenson.viand.models.ChatMessage;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.network.GeminiClient;
import com.annabenson.viand.network.GeminiRequest;
import com.annabenson.viand.network.GeminiResponse;
import com.annabenson.viand.network.GeminiService;
import com.annabenson.viand.network.RecipeSearchResponse;
import com.annabenson.viand.network.RetrofitClient;
import com.annabenson.viand.network.SpoonacularService;

import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryActivity extends AppCompatActivity {

    // Set to true to use Spoonacular test responses instead of Gemini
    private static final boolean TEST_MODE = true;

    private static final String SYSTEM_PROMPT =
            "You are Vivian, a friendly cooking assistant. " +
            "When the user tells you what ingredients they have, suggest practical recipes they can make. " +
            "Give each suggestion a name, a one-sentence description, and 2-3 key steps. " +
            "If the user asks follow-up questions about a recipe, give more detail. " +
            "If they're missing a key ingredient, suggest a simple substitution. " +
            "Keep responses warm, concise, and focused on cooking.";

    private RecyclerView chatRecyclerView;
    private TextInputEditText messageInput;
    private Button sendButton;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final List<GeminiRequest.Content> conversationHistory = new ArrayList<>();

    private GeminiService geminiService;
    private SpoonacularService spoonacularService;
    private boolean isWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_pantry);

        Toolbar toolbar = findViewById(com.annabenson.viand.R.id.pantryToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView = findViewById(com.annabenson.viand.R.id.chatRecyclerView);
        messageInput = findViewById(com.annabenson.viand.R.id.messageInput);
        sendButton = findViewById(com.annabenson.viand.R.id.sendButton);

        geminiService = GeminiClient.getInstance().create(GeminiService.class);
        spoonacularService = RetrofitClient.getInstance().create(SpoonacularService.class);

        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        String opening = TEST_MODE
                ? "Hi, I'm Vivian! (Test Mode) Send me anything and I'll show you some Chicken Noodle Soup recipes from Spoonacular."
                : "Hi, I'm Vivian! Tell me what ingredients you have in your pantry or fridge and I'll suggest some recipes you can make.";
        addAiMessage(opening);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        if (isWaiting) return;

        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        messageInput.setText("");

        messages.add(new ChatMessage(ChatMessage.Type.USER, text));
        chatAdapter.notifyItemInserted(messages.size() - 1);

        ChatMessage loadingMsg = new ChatMessage(ChatMessage.Type.LOADING, "");
        messages.add(loadingMsg);
        int loadingIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(loadingIndex);
        scrollToBottom();

        isWaiting = true;
        sendButton.setEnabled(false);

        if (TEST_MODE) {
            sendTestModeRequest(loadingIndex);
        } else {
            sendGeminiRequest(text, loadingIndex);
        }
    }

    private void sendTestModeRequest(int loadingIndex) {
        spoonacularService.searchRecipes("Chicken Noodle Soup", 5, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        isWaiting = false;
                        sendButton.setEnabled(true);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            if (results == null || results.isEmpty()) {
                                addAiMessage("Test mode: Spoonacular returned no results.");
                                return;
                            }
                            messages.add(new ChatMessage(results));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            scrollToBottom();
                        } else {
                            addAiMessage("Test mode: Spoonacular error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        isWaiting = false;
                        sendButton.setEnabled(true);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);
                        addAiMessage("Test mode: Network error: " + t.getMessage());
                    }
                });
    }

    private void sendGeminiRequest(String text, int loadingIndex) {
        conversationHistory.add(new GeminiRequest.Content("user",
                Collections.singletonList(new GeminiRequest.Part(text))));

        GeminiRequest request = new GeminiRequest(SYSTEM_PROMPT,
                new ArrayList<>(conversationHistory));

        geminiService.generateContent(BuildConfig.GEMINI_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                                           Response<GeminiResponse> response) {
                        isWaiting = false;
                        sendButton.setEnabled(true);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);

                        if (response.isSuccessful() && response.body() != null) {
                            String reply = response.body().getText();
                            if (reply == null || reply.isEmpty()) reply = "Sorry, I couldn't generate a response.";
                            addAiMessage(reply);
                            conversationHistory.add(new GeminiRequest.Content("model",
                                    Collections.singletonList(new GeminiRequest.Part(reply))));
                        } else {
                            String errorDetail = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorDetail = response.errorBody().string();
                                }
                            } catch (IOException e) {
                                errorDetail = e.getMessage();
                            }
                            Log.e("Vivian", "API error " + response.code() + ": " + errorDetail);
                            addAiMessage("Error " + response.code() + ": " + errorDetail);
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        isWaiting = false;
                        sendButton.setEnabled(true);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);
                        addAiMessage("Network error: " + t.getMessage());
                    }
                });
    }

    private void addAiMessage(String text) {
        messages.add(new ChatMessage(ChatMessage.Type.AI, text));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatRecyclerView.post(() ->
                chatRecyclerView.smoothScrollToPosition(messages.size() - 1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

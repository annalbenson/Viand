package com.annabenson.viand;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.annabenson.viand.network.GeminiClient;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryActivity extends AppCompatActivity {

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
    private boolean isWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        Toolbar toolbar = findViewById(R.id.pantryToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        geminiService = GeminiClient.getInstance().create(GeminiService.class);

        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Opening message
        addAiMessage("Hi, I'm Vivian! Tell me what ingredients you have in your pantry " +
                "or fridge and I'll suggest some recipes you can make.");

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

        // Add user message to UI and history
        messages.add(new ChatMessage(ChatMessage.Type.USER, text));
        chatAdapter.notifyItemInserted(messages.size() - 1);

        conversationHistory.add(new GeminiRequest.Content("user",
                Collections.singletonList(new GeminiRequest.Part(text))));

        // Add loading indicator
        ChatMessage loadingMsg = new ChatMessage(ChatMessage.Type.LOADING, "");
        messages.add(loadingMsg);
        int loadingIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(loadingIndex);
        scrollToBottom();

        isWaiting = true;
        sendButton.setEnabled(false);

        GeminiRequest request = new GeminiRequest(SYSTEM_PROMPT,
                new ArrayList<>(conversationHistory));

        geminiService.generateContent(BuildConfig.GEMINI_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                                           Response<GeminiResponse> response) {
                        isWaiting = false;
                        sendButton.setEnabled(true);

                        // Remove loading indicator
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);

                        if (response.isSuccessful() && response.body() != null) {
                            String reply = response.body().getText();
                            if (reply == null || reply.isEmpty()) reply = "Sorry, I couldn't generate a response.";

                            addAiMessage(reply);

                            // Add AI response to history for context in follow-ups
                            conversationHistory.add(new GeminiRequest.Content("model",
                                    Collections.singletonList(new GeminiRequest.Part(reply))));
                        } else {
                            addAiMessage("Sorry, something went wrong (error " + response.code() + "). Try again.");
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

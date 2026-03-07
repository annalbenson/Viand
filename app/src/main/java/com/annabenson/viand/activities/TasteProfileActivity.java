package com.annabenson.viand.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.annabenson.viand.R;
import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.engine.TasteEngine;
import com.annabenson.viand.models.TasteTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TasteProfileActivity extends AppCompatActivity {

    private LinearLayout sliderContainer;
    private DatabaseHandler databaseHandler;
    private int userId;

    private final Map<String, SeekBar> seekBars = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taste_profile);

        Toolbar toolbar = findViewById(R.id.tasteProfileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Taste Profile");
        }

        sliderContainer = findViewById(R.id.sliderContainer);
        Button saveButton = findViewById(R.id.saveTasteProfileButton);

        databaseHandler = new DatabaseHandler(this);
        userId = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(LoginActivity.KEY_USER_ID, -1);

        // Load existing scores into a lookup map
        List<TasteTag> profile = databaseHandler.loadCuisineProfile(userId);
        Map<String, Float> scoreMap = new HashMap<>();
        for (TasteTag tag : profile) {
            scoreMap.put(tag.getTag(), tag.getScore());
        }

        // Build one slider row per cuisine in the graph
        for (String cuisine : TasteEngine.CUISINE_GRAPH.keySet()) {
            Float existing = scoreMap.get(cuisine);
            float score = existing != null ? existing : 0f;
            addSliderRow(cuisine, score);
        }

        saveButton.setOnClickListener(v -> {
            for (Map.Entry<String, SeekBar> entry : seekBars.entrySet()) {
                databaseHandler.setTasteScore(
                        userId, entry.getKey(), "cuisine", entry.getValue().getProgress());
            }
            finish();
        });
    }

    private void addSliderRow(String cuisine, float currentScore) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = dpToPx(8);
        rowParams.setMargins(0, 0, 0, margin);
        row.setLayoutParams(rowParams);

        // Cuisine label
        TextView label = new TextView(this);
        label.setText(cuisine);
        label.setTextSize(14f);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                dpToPx(130), LinearLayout.LayoutParams.WRAP_CONTENT);
        label.setLayoutParams(labelParams);
        row.addView(label);

        // SeekBar (0–10)
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(10);
        seekBar.setProgress(Math.min(10, Math.max(0, (int) currentScore)));
        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        seekParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        seekBar.setLayoutParams(seekParams);
        seekBars.put(cuisine, seekBar);
        row.addView(seekBar);

        // Value display
        TextView valueText = new TextView(this);
        valueText.setText(String.valueOf(seekBar.getProgress()));
        valueText.setTextSize(14f);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                dpToPx(30), LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.gravity = Gravity.CENTER_VERTICAL;
        valueText.setLayoutParams(valueParams);
        row.addView(valueText);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                valueText.setText(String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        sliderContainer.addView(row);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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

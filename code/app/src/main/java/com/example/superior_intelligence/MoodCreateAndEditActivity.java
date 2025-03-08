package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MoodCreateAndEditActivity extends AppCompatActivity {

    // Title
    private EditText headerTitle;

    // Emotion
    private ImageView emotionArrow;
    private Spinner emotionSpinner;
    private TextView selectedMood;

    // Situation
    private ImageView situationArrow;
    private Spinner situationSpinner;
    private TextView selectedSituation;

    // Emoji
    private ImageButton emojiButton;
    private CheckBox includeEmojiCheckbox;

    // Buttons
    private ImageButton backButton;
    private ImageButton addPhotoButton;
    private LinearLayout confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        // 1) Find views
        headerTitle = findViewById(R.id.header_title);

        emotionArrow = findViewById(R.id.emotion_arrow);
        emotionSpinner = findViewById(R.id.emotion_spinner);
        selectedMood = findViewById(R.id.selected_mood);

        situationArrow = findViewById(R.id.situation_arrow);
        situationSpinner = findViewById(R.id.situation_spinner);
        selectedSituation = findViewById(R.id.selected_situation);

        emojiButton = findViewById(R.id.emoji_button);
        includeEmojiCheckbox = findViewById(R.id.include_emoji_checkbox);

        backButton = findViewById(R.id.back_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        confirmButton = findViewById(R.id.confirm_button);

        // 2) Set up the Spinners
        setupEmotionSpinner();
        setupSituationSpinner();

        // 3) Arrow clicks: Show and immediately open the spinner
        emotionArrow.setOnClickListener(v -> {
            if (emotionSpinner.getVisibility() == View.GONE) {
                emotionSpinner.setVisibility(View.VISIBLE);
                emotionSpinner.performClick();
            } else {
                emotionSpinner.setVisibility(View.GONE);
            }
        });

        situationArrow.setOnClickListener(v -> {
            if (situationSpinner.getVisibility() == View.GONE) {
                situationSpinner.setVisibility(View.VISIBLE);
                situationSpinner.performClick();
            } else {
                situationSpinner.setVisibility(View.GONE);
            }
        });

        // 4) Back button: Return to HomeActivity
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MoodCreateAndEditActivity.this, HomeActivity.class));
            finish();
        });

        // 5) Photo button: Open PhotoActivity
        addPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodCreateAndEditActivity.this, PhotoActivity.class);
            startActivity(intent);
        });

        // 6) Confirm button: Return to HomeActivity with the "myposts" tab selected
        confirmButton.setOnClickListener(v -> {
            String eventTitle = headerTitle.getText().toString().trim();
            String mood = selectedMood.getText().toString();
            String situation = selectedSituation.getText().toString();
            boolean includeEmoji = includeEmojiCheckbox.isChecked();

            // TODO: Save or pass these values as needed

            Intent intent = new Intent(MoodCreateAndEditActivity.this, HomeActivity.class);
            intent.putExtra("selectedTab", "myposts");
            startActivity(intent);
            finish();
        });
    }

    // ---------- HELPER METHODS ----------

    private void setupEmotionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.emotional_state_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(adapter);
        emotionSpinner.setVisibility(View.GONE);

        // Always update bubbles and emoji when a selection is made
        emotionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosenEmotion = parent.getItemAtPosition(position).toString();
                selectedMood.setText(chosenEmotion);
                updateEmojiIcon(chosenEmotion);
                // Hide spinner after selection
                emotionSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSituationSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.social_situation_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        situationSpinner.setAdapter(adapter);
        situationSpinner.setVisibility(View.GONE);

        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosenSituation = parent.getItemAtPosition(position).toString();
                selectedSituation.setText(chosenSituation);
                situationSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Update the emoji icon based on the chosen mood.
     * Adjust the drawables and mood strings as needed.
     */
    private void updateEmojiIcon(String mood) {
        if (mood.equalsIgnoreCase("anger")) {
            emojiButton.setImageResource(R.drawable.angry_icon);
        } else if (mood.equalsIgnoreCase("happiness")) {
            emojiButton.setImageResource(R.drawable.happy_icon);
        } else if (mood.equalsIgnoreCase("sadness")) {
            emojiButton.setImageResource(R.drawable.sad_icon);
        } else {
            // Default icon if no match is found
            emojiButton.setImageResource(R.drawable.happy_icon);
        }
    }
}

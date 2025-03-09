package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoodCreateAndEditActivity extends AppCompatActivity {

    // Title
    private EditText headerTitle;

    // Emotion
    private ImageView emotionArrow;
    private Spinner emotionSpinner;
    private TextView selectedMood;
    private boolean isEmotionSelected = false; // Tracks if an emotion is selected

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
    private FrameLayout confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        // 1) Find views
        headerTitle = findViewById(R.id.mood_event_title);

        emotionArrow = findViewById(R.id.emotion_arrow);
        emotionSpinner = findViewById(R.id.emotion_spinner);
        selectedMood = findViewById(R.id.selected_mood);

        situationArrow = findViewById(R.id.situation_arrow);
        situationSpinner = findViewById(R.id.situation_spinner);
        selectedSituation = findViewById(R.id.selected_situation);

        emojiButton = findViewById(R.id.emoji_button);
        includeEmojiCheckbox = findViewById(R.id.include_emoji_checkbox);

        backButton = findViewById(R.id.mood_events_list_back_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        confirmButton = findViewById(R.id.confirm_mood_create_button);

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

        // 6) Confirm button: Ensure emotion is selected before proceeding
        confirmButton.setOnClickListener(v -> handleConfirmClick());
    }

    /**
     * Ensures that an emotional state is selected before confirming the mood event.
     * If no emotional state is selected, a toast message is displayed.
     */
    private void handleConfirmClick() {
        if (!isEmotionSelected) {
            // Show a Toast message instead of an AlertDialog
            Toast.makeText(this, "An emotional state must be selected.", Toast.LENGTH_SHORT).show();
        } else {
            // Proceed to HomeActivity if an emotion is selected
            Event newEvent = createNewEvent();
            Intent intent = new Intent(MoodCreateAndEditActivity.this, HomeActivity.class);
            intent.putExtra("selectedTab", "myposts");
            intent.putExtra("newEvent", newEvent);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Creates a new event object with selected details.
     */
    private Event createNewEvent() {
        String eventTitle = headerTitle.getText().toString().trim();
        String eventDate = "Today";
        String overlayColor = "#FFD700";
        String imageUrl = "";
        int emojiResource = includeEmojiCheckbox.isChecked() ? updateEmojiIcon(selectedMood.getText().toString()) : 0;
        boolean isFollowed = false;
        boolean isMyPost = true;

        return new Event(eventTitle, eventDate, overlayColor, imageUrl, emojiResource, isFollowed, isMyPost);
    }

    /**
     * Sets up the emotion spinner and updates state when selected.
     */
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

                // Ensure the Confirm button is enabled when an emotion is selected
                isEmotionSelected = true;

                // Hide spinner after selection
                emotionSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Sets up the social situation spinner.
     */
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
     */
    private int updateEmojiIcon(String mood) {
        int emojiResId;

        if (mood.equalsIgnoreCase("anger")) {
            emojiResId = R.drawable.angry_icon;
        } else if (mood.equalsIgnoreCase("happiness")) {
            emojiResId = R.drawable.happy_icon;
        } else if (mood.equalsIgnoreCase("sadness")) {
            emojiResId = R.drawable.sad_icon;
        } else {
            emojiResId = R.drawable.happy_icon; // Default icon
        }

        emojiButton.setImageResource(emojiResId);
        return emojiResId;
    }
}

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;

public class MoodCreateAndEditActivity extends AppCompatActivity {

    // Title
    private EditText headerTitle;

    // Emotion
    private ImageView emotionArrow;
    private Spinner emotionSpinner;
    private TextView selectedMood;
    private boolean isEmotionSelected = false; // Tracks if an emotion is selected
    // Explanation
    private EditText triggerExplanation;
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
    private String imageUrl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        // 1) Find views
        headerTitle = findViewById(R.id.mood_event_title);

        emotionArrow = findViewById(R.id.emotion_arrow);
        emotionSpinner = findViewById(R.id.emotion_spinner);
        selectedMood = findViewById(R.id.selected_mood);

        triggerExplanation = findViewById(R.id.trigger_response);

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
            photoActivityLauncher.launch(intent); // Launch PhotoActivity expecting a result
        });

        // 6) Confirm button: Ensure emotion is selected before proceeding
        confirmButton.setOnClickListener(v -> handleConfirmClick());
    }

    // ActivityResultLauncher to receive image document ID from PhotoActivity
    private final ActivityResultLauncher<Intent> photoActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String uploadedImageDocID = result.getData().getStringExtra("imageDocID");
                    if (uploadedImageDocID != null) {
                        Log.d("MoodCreateAndEditActivity", "Received image ID: " + uploadedImageDocID);
                        imageUrl = uploadedImageDocID; // Store it for when we create the event
                    }
                }
            });

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
        int emojiResource = includeEmojiCheckbox.isChecked() ? updateEmojiIcon(selectedMood.getText().toString()) : 0;
        boolean isFollowed = false;
        boolean isMyPost = true;
        String mood = selectedMood.getText().toString();
        String moodExplanation = triggerExplanation.getText().toString();
        String situation = selectedSituation.getText().toString();
        String finalImageUrl = (imageUrl != null) ? imageUrl : "";
        User user = User.getInstance();

        return new Event(eventTitle, eventDate, overlayColor, finalImageUrl, emojiResource, isFollowed, isMyPost, mood, moodExplanation, situation, user.getUsername());
    }

    /**
     * Sets up the emotion spinner and updates state when selected.
     */
    private void setupEmotionSpinner() {
        ArrayList<String> emotions = new ArrayList<>();
        emotions.add("Select a Mood"); // Placeholder item
        Collections.addAll(emotions, getResources().getStringArray(R.array.emotional_state_list));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emotions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundGreen));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(adapter);
        emotionSpinner.setVisibility(View.GONE);

        emotionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Do nothing for placeholder item
                    isEmotionSelected = false;
                    return;
                }

                String chosenEmotion = parent.getItemAtPosition(position).toString();
                selectedMood.setText(chosenEmotion);
                updateEmojiIcon(chosenEmotion);
                isEmotionSelected = true;

                emotionSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set default selection to the placeholder (to prevent preselection of first real emotion)
        emotionSpinner.setSelection(0, false);
    }
    /**
     * Sets up the social situation spinner.
     */
    private void setupSituationSpinner() {
        ArrayList<String> situations = new ArrayList<>();
        situations.add("Select a Situation"); // Placeholder item
        Collections.addAll(situations, getResources().getStringArray(R.array.social_situation_list));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, situations) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        situationSpinner.setAdapter(adapter);
        situationSpinner.setVisibility(View.GONE);

        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Do nothing for placeholder item
                    return;
                }

                String chosenSituation = parent.getItemAtPosition(position).toString();
                selectedSituation.setText(chosenSituation);
                situationSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set default selection to the placeholder (to prevent preselection of first real situation)
        situationSpinner.setSelection(0, false);
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

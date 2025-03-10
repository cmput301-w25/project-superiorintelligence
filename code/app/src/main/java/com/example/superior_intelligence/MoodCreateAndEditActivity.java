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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodCreateAndEditActivity extends AppCompatActivity {

    // Title
    EditText headerTitle;

    // Emotion
    private ImageView emotionArrow;
    private Spinner emotionSpinner;
    TextView selectedMood;
    boolean isEmotionSelected = false; // Tracks if an emotion is selected
    // Explanation
    EditText triggerExplanation;
    // Situation
    private ImageView situationArrow;
    private Spinner situationSpinner;
    TextView selectedSituation;

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

        // find views
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

        // set up the spinners
        setupEmotionSpinner();
        setupSituationSpinner();

        // Arrow clicks show and immediately open the spinner
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


        // Preset mood's old info
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String mood = intent.getStringExtra("mood");
        String reason = intent.getStringExtra("reason");
        String situation = intent.getStringExtra("socialSituation");

        headerTitle.setText(title);
         /* NOT WORKING
        selectedMood.setText(mood);
        triggerExplanation.setText(reason);
        selectedSituation.setText(situation);
        */

        // Back button returns to HomeActivity
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MoodCreateAndEditActivity.this, HomeActivity.class));
            finish();
        });

        // Photo button opens PhotoActivity
        addPhotoButton.setOnClickListener(v -> {
            Intent photoIntent = new Intent(MoodCreateAndEditActivity.this, PhotoActivity.class);
            photoActivityLauncher.launch(photoIntent);
        });

        // Confirm button but ensures emotion is selected before proceeding
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
    void handleConfirmClick() {
        if (!isEmotionSelected) {
            Toast.makeText(this, "An emotional state must be selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        String explanation = triggerExplanation.getText().toString().trim();

        // Validate explanation if provided
        if (!explanation.isEmpty() && !isValidExplanation(explanation)) {
            Toast.makeText(this, "Reason must be max 20 characters or 3 words.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed if valid
        Event newEvent = createNewEvent();
        Intent intent = new Intent(MoodCreateAndEditActivity.this, HomeActivity.class);
        intent.putExtra("selectedTab", "myposts");
        intent.putExtra("newEvent", newEvent);
        startActivity(intent);
        finish();
    }

    /**
     * Checks if the explanation is within the allowed limits.
     * - Max: 20 characters OR 3 words.
     * - Allows empty input (optional field).
     */
    private boolean isValidExplanation(String explanation) {
        if (explanation.isEmpty()) {
            return true; // Empty input is allowed
        }

        if (explanation.length() > 20) {
            return false; // Exceeds 20 character limit
        }

        String[] words = explanation.split("\\s+");
        return words.length <= 3; // Ensure max 3 words
    }


    /**
     * Creates a new event object with selected details.
     */
    Event createNewEvent() {
        String eventTitle = headerTitle.getText().toString().trim();
        if (eventTitle.isEmpty()) {
            eventTitle = "Untitled"; // Default title
        }
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        String overlayColor = getOverlayColorForMood(selectedMood.getText().toString()); // Dynamic based on mood
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
    int updateEmojiIcon(String mood) {
        int emojiResId;

        if (mood.equalsIgnoreCase("anger")) {
            emojiResId = R.drawable.angry_icon;
        } else if (mood.equalsIgnoreCase("happiness")) {
            emojiResId = R.drawable.happy_icon;
        } else if (mood.equalsIgnoreCase("sadness")) {
            emojiResId = R.drawable.sad_icon;
        } else if (mood.equalsIgnoreCase("disgust")){
            emojiResId = R.drawable.disgust;
        } else if (mood.equalsIgnoreCase("confusion")){
            emojiResId = R.drawable.confusion;
        } else if (mood.equalsIgnoreCase("fear")){
            emojiResId = R.drawable.fear;
        } else if (mood.equalsIgnoreCase("shame")){
            emojiResId = R.drawable.shame;
        } else if (mood.equalsIgnoreCase("surprise")){
            emojiResId = R.drawable.surprise;
        } else {
            emojiResId = R.drawable.happy_icon; // Default icon
        }

        emojiButton.setImageResource(emojiResId);
        return emojiResId;
    }

    private String getOverlayColorForMood(String mood) {
        switch (mood.toLowerCase()) {
            case "anger":
                return "#FF6347"; // Tomato Red
            case "happiness":
                return "#FFD700"; // Yellow (current one)
            case "sadness":
                return "#87CEEB"; // Sky Blue
            case "fear":
                return "#778899"; // Slate Gray
            case "shame":
                return "#FFB6C1"; // Light Pink
            case "confusion":
                return "#CC0099"; // Purple
            case "surprise":
                return "#FFA500"; // Orange
            case "disgust":
                return "#98FB98"; // Pale Green
            default:
                return "#FFD700"; // Default to Yellow
        }
    }

}

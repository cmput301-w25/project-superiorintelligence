package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;

public class MoodCreateAndEditActivity extends AppCompatActivity {

    private EditText moodTitle;
    private CardView emotionSelector, socialSituationSelector;
    private Spinner emotionDropdown, emotionDropdown2;
    private TextView emotionText, socialSituationText;
    private boolean isEmotionSelected = false;
    private boolean isSocialSituationSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        // Initialize editable title
        moodTitle = findViewById(R.id.mood_event_title);

        // Ensure title is not empty when focus is lost
        moodTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String titleText = moodTitle.getText().toString().trim();
                if (titleText.isEmpty()) {
                    moodTitle.setText("Untitled"); // Reset to default title if empty
                }
            }
        });

        // Allow user to confirm title by pressing Enter
        moodTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                moodTitle.clearFocus(); // Remove focus when Enter is pressed
            }
            return false;
        });

        // Initialize UI components for first spinner (Emotional State)
        emotionSelector = findViewById(R.id.cardView);
        emotionDropdown = findViewById(R.id.emotion_dropdown);
        emotionText = findViewById(R.id.emotional_state_text);

        // Initialize UI components for second spinner (Social Situation)
        socialSituationSelector = findViewById(R.id.social_situation_banner);
        emotionDropdown2 = findViewById(R.id.emotion_dropdown2);
        socialSituationText = findViewById(R.id.social_situation_text);

        // Initialize Confirm Button
        FrameLayout confirmButton = findViewById(R.id.confirm_mood_create_button);
        ImageButton confirmIcon = confirmButton.findViewById(R.id.confirm_button_icon); // Get the ImageButton inside FrameLayout

        // Load emotions into the first Spinner
        ArrayAdapter<CharSequence> emotionAdapter = new ArrayAdapter<>(this,
                R.layout.all_dropdown_options,
                getResources().getStringArray(R.array.emotional_state_list));
        emotionAdapter.setDropDownViewResource(R.layout.all_dropdown_options);
        emotionDropdown.setAdapter(emotionAdapter);

        // Load social situations into the second Spinner
        ArrayAdapter<CharSequence> socialSituationAdapter = new ArrayAdapter<>(this,
                R.layout.all_dropdown_options,
                getResources().getStringArray(R.array.social_situation_list));
        socialSituationAdapter.setDropDownViewResource(R.layout.all_dropdown_options);
        emotionDropdown2.setAdapter(socialSituationAdapter);

        // Hide spinners initially
        emotionDropdown.setVisibility(View.GONE);
        emotionDropdown2.setVisibility(View.GONE);

        // Toggle first dropdown when clicking "Emotional State"
        emotionSelector.setOnClickListener(v -> {
            if (emotionDropdown.getVisibility() == View.GONE) {
                emotionDropdown.setVisibility(View.VISIBLE);
            } else {
                emotionDropdown.setVisibility(View.GONE);
            }
        });

        // Toggle second dropdown when clicking "Social Situation"
        socialSituationSelector.setOnClickListener(v -> {
            if (emotionDropdown2.getVisibility() == View.GONE) {
                emotionDropdown2.setVisibility(View.VISIBLE);
            } else {
                emotionDropdown2.setVisibility(View.GONE);
            }
        });

        // Handle selection for first dropdown (Emotional State)
        emotionDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedEmotion = parent.getItemAtPosition(position).toString();
                if (!selectedEmotion.equals("Emotional State")) {
                    emotionText.setText(selectedEmotion);
                    isEmotionSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isEmotionSelected = false;
            }
        });

        // Confirm button click listener (FrameLayout Click)
        confirmButton.setOnClickListener(v -> handleConfirmClick());

        // Confirm button click listener (ImageButton Click)
        confirmIcon.setOnClickListener(v -> handleConfirmClick());

        // Handle selection for the second dropdown (Social Situation)
        emotionDropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSocialSituation = parent.getItemAtPosition(position).toString();

                if (isSocialSituationSelected && socialSituationText.getText().toString().equals(selectedSocialSituation)) {
                    socialSituationText.setText("Current social situation");
                    isSocialSituationSelected = false;
                } else {
                    socialSituationText.setText(selectedSocialSituation);
                    isSocialSituationSelected = true;
                }
                emotionDropdown2.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                socialSituationText.setText("Current social situation");
                isSocialSituationSelected = false;
            }
        });

        // --- Navigation Buttons Implementation ---

        // Back Button: Navigate back to HomeActivity
        ImageButton backButton = findViewById(R.id.mood_events_list_back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodCreateAndEditActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Add Photo Button: Launch PhotoActivity
        ImageButton addPhotoButton = findViewById(R.id.add_photo_button);
        addPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodCreateAndEditActivity.this, PhotoActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Handles Confirm button click event.
     */
    private void handleConfirmClick() {
        if (!isEmotionSelected) {
            // Show AlertDialog when no emotion is selected
            AlertDialog.Builder builder = new AlertDialog.Builder(MoodCreateAndEditActivity.this);
            builder.setTitle("Selection Required")
                    .setMessage("Please select an emotional state before confirming.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Intent intent = new Intent(MoodCreateAndEditActivity.this, HomeActivity.class);
            intent.putExtra("selectedTab", "myposts");
            startActivity(intent);
            finish();
        }
    }
}

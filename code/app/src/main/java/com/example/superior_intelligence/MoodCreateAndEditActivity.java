package com.example.superior_intelligence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

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

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Double lat = null;
    private Double lng = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        CheckBox mapCheckbox = findViewById(R.id.include_map_checkbox);
        mapCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If the user just checked the box, request location permission & fetch lat/lng
                handleLocationClick();
            } else {
                // If unchecked, reset lat/lng if you want
                lat = null;
                lng = null;
            }
        });
        setupEmotionSpinner();
        setupSituationSpinner();

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
        triggerExplanation.setText(reason);
         /* NOT WORKING
        selectedMood.setText(mood);

        selectedSituation.setText(situation);
        */

        // Back button returns to HomeActivity

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MoodCreateAndEditActivity.this, HomeActivity.class));
            finish();
        });

        addPhotoButton.setOnClickListener(v -> {
            Intent photoIntent = new Intent(MoodCreateAndEditActivity.this, PhotoActivity.class);
            photoActivityLauncher.launch(photoIntent);
        });

        confirmButton.setOnClickListener(v -> handleConfirmClick());
    }

    private final ActivityResultLauncher<Intent> photoActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String uploadedImageDocID = result.getData().getStringExtra("imageDocID");
                    if (uploadedImageDocID != null) {
                        Log.d("MoodCreateAndEditActivity", "Received image ID: " + uploadedImageDocID);
                        imageUrl = uploadedImageDocID;
                    }
                }
            });

    private void handleConfirmClick() {
        if (!isEmotionSelected) {
            Toast.makeText(this, "An emotional state must be selected.", Toast.LENGTH_SHORT).show();

        // Validate explanation, etc. ...
        String explanation = triggerExplanation.getText().toString().trim();

        // Validate explanation if provided
        if (!explanation.isEmpty() && !isValidExplanation(explanation)) {
            Toast.makeText(this, "Reason must be max 20 characters or 3 words.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the new Event object
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
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        String overlayColor = "#FFD700";
        int emojiResource = includeEmojiCheckbox.isChecked() ? updateEmojiIcon(selectedMood.getText().toString()) : 0;
        boolean isFollowed = false;
        boolean isMyPost = true;
        String mood = selectedMood.getText().toString();
        String moodExplanation = triggerExplanation.getText().toString();
        String situation = selectedSituation.getText().toString();
        String finalImageUrl = (imageUrl != null) ? imageUrl : "";
        User user = User.getInstance();


        return new Event(eventTitle, eventDate, overlayColor, finalImageUrl,
                emojiResource, isFollowed, isMyPost,
                mood, moodExplanation, situation, user.getUsername(),
                lat, lng);
    }


    private void setupEmotionSpinner() {
        ArrayList<String> emotions = new ArrayList<>();
        emotions.add("Select a Mood");
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
        emotionSpinner.setSelection(0, false);
    }


    private void setupSituationSpinner() {
        ArrayList<String> situations = new ArrayList<>();
        situations.add("Select a Situation");
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
                    return;
                }
                String chosenSituation = parent.getItemAtPosition(position).toString();
                selectedSituation.setText(chosenSituation);
                situationSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        situationSpinner.setSelection(0, false);
    }


    private int updateEmojiIcon(String mood) {
        int emojiResId;
        if (mood.equalsIgnoreCase("anger")) {
            emojiResId = R.drawable.angry_icon;
        } else if (mood.equalsIgnoreCase("happiness")) {
            emojiResId = R.drawable.happy_icon;
        } else if (mood.equalsIgnoreCase("sadness")) {
            emojiResId = R.drawable.sad_icon;
        } else {
            emojiResId = R.drawable.happy_icon; // default
        }
        emojiButton.setImageResource(emojiResId);
        return emojiResId;
    }

    private void handleLocationClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Toast.makeText(this, "Location: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleLocationClick();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

package com.example.superior_intelligence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location; // Must import this
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import java.util.UUID;

/**
 * Activity for creating/editing mood events with title, emotion, situation, explanation, photo, and location.
 */
public class MoodCreateAndEditActivity extends AppCompatActivity implements PostStatusFragment.PostStatusDialogListener{

    // Unique ID for Mood Event
    private String eventID;
    private String eventDate;

    // Passing an event
    Event currentEvent;

    // Title
    EditText headerTitle;

    // Post status
    boolean postPublicStatus;

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
    TextView selectedSituation, explanationCounter;

    // Emoji
    private ImageButton emojiButton;
    CheckBox includeEmojiCheckbox;

    // Buttons
    private ImageButton backButton;
    private ImageButton addPhotoButton;
    private FrameLayout confirmButton;
    private String imageUrl = null;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Double lat = 0.0;
    private Double lng = 0.0;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }


    // PhotoActivity launcher for receiving the uploaded imageDocID
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

    /**
     * Initializes activity and sets up UI components for mood event creation/editing.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Find Views
        headerTitle = findViewById(R.id.mood_event_title);

        emotionArrow = findViewById(R.id.emotion_arrow);
        emotionSpinner = findViewById(R.id.emotion_spinner);
        selectedMood = findViewById(R.id.selected_mood);
        explanationCounter = findViewById(R.id.explanation_counter);
        triggerExplanation = findViewById(R.id.trigger_response);

        setupTriggerExplanationWatcher();

        situationArrow = findViewById(R.id.situation_arrow);
        situationSpinner = findViewById(R.id.situation_spinner);
        selectedSituation = findViewById(R.id.selected_situation);

        emojiButton = findViewById(R.id.emoji_button);
        includeEmojiCheckbox = findViewById(R.id.include_emoji_checkbox);

        backButton = findViewById(R.id.mood_events_list_back_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        confirmButton = findViewById(R.id.confirm_mood_create_button);

        // Map checkbox toggles location
        CheckBox mapCheckbox = findViewById(R.id.include_map_checkbox);
        mapCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleLocationClick();
            } else {
                lat = 0.0;
                lng = 0.0;
            }
        });

        // Setup spinners
        setupEmotionSpinner();
        setupSituationSpinner();

        // Arrow icons to open spinners
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

        // Retrieve whole Event object if passed for editing
        Intent intent = getIntent();
        currentEvent = (Event) intent.getSerializableExtra("event"); // Get full Event object

        if (currentEvent != null) {
            // Pre-fill fields using getters
            eventID = currentEvent.getID();
            eventDate = currentEvent.getDate(); // Preserved date
            imageUrl = currentEvent.getImageUrl();

            headerTitle.setText(currentEvent.getTitle());
            triggerExplanation.setText(currentEvent.getMoodExplanation());

            // Set mood spinner
            if (currentEvent.getMood() != null) {
                ArrayAdapter<String> moodAdapter = (ArrayAdapter<String>) emotionSpinner.getAdapter();
                selectedMood.setText(currentEvent.getMood());
                // int moodPosition = moodAdapter.getPosition(currentEvent.getMood());
                // if (moodPosition >= 0) emotionSpinner.setSelection(moodPosition);
            }

            // Set situation spinner
            if (currentEvent.getSituation() != null) {
                ArrayAdapter<String> situationAdapter = (ArrayAdapter<String>) situationSpinner.getAdapter();
                selectedSituation.setText(currentEvent.getSituation());
                // int situationPosition = situationAdapter.getPosition(currentEvent.getSituation());
                // if (situationPosition >= 0) situationSpinner.setSelection(situationPosition);
            }
        }

        // Back button returns to HomeActivity
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MoodCreateAndEditActivity.this, HomeActivity.class));
            finish();
        });

        // Add photo button
        addPhotoButton.setOnClickListener(v -> {
            Intent photoIntent = new Intent(MoodCreateAndEditActivity.this, PhotoActivity.class);
            photoActivityLauncher.launch(photoIntent);
        });

        // Confirm button
        confirmButton.setOnClickListener(v -> handleConfirmClick()); // Existing logic
    }

    /**
     * Attempt to retrieve location, prompting for permission if needed.
     */
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
                        Toast.makeText(this,
                                "Location: " + lat + ", " + lng,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Location error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    /**
     * Handles location permission request result.
     * @param requestCode Request code
     * @param permissions Requested permissions
     * @param grantResults Grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
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

    /**
     * Sets up emotion spinner with mood options.
     * Includes selection listener.
     */
    private void setupEmotionSpinner() {
        ArrayList<String> emotions = new ArrayList<>();
        emotions.add("Select a Mood");
        // Make sure you have an array resource named 'emotional_state_list' in your res/values/strings.xml
        Collections.addAll(emotions, getResources().getStringArray(R.array.emotional_state_list));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                emotions
        );
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
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        // Start spinner at the "Select a Mood" placeholder
        emotionSpinner.setSelection(0, false);
    }

    /**
     * Sets up situation spinner with context options.
     * Includes selection listener.
     */
    private void setupSituationSpinner() {
        ArrayList<String> situations = new ArrayList<>();
        situations.add("Select a Situation");
        // Make sure you have an array resource named 'social_situation_list'
        Collections.addAll(situations, getResources().getStringArray(R.array.social_situation_list));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                situations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        situationSpinner.setAdapter(adapter);
        situationSpinner.setVisibility(View.GONE);

        situationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id
            ) {
                if (position == 0) {
                    return;
                }
                String chosenSituation = parent.getItemAtPosition(position).toString();
                selectedSituation.setText(chosenSituation);
                situationSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        // Start spinner at the "Select a Situation" placeholder
        situationSpinner.setSelection(0, false);
    }

    /**
     * Validates inputs and initiates event creation/update.
     * Shows errors for invalid inputs.
     */
    void handleConfirmClick() {
        if (!isEmotionSelected) {
            Toast.makeText(this, "An emotional state must be selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate explanation
        String explanation = triggerExplanation.getText().toString().trim();
        if (!explanation.isEmpty() && !isValidExplanation(explanation)) {
            Toast.makeText(this, "Reason must be max 200.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = headerTitle.getText().toString().trim();

        Database database = new Database(); // Instance to handle Firestore

        if (eventID == null || eventID.isEmpty()) {
            // get public/private post status
            PostStatusFragment postStatusDialogFragment = new PostStatusFragment(null);
            postStatusDialogFragment.show(getSupportFragmentManager(), "PostStatusDialog");

        } else {
            // UPDATE existing Event scenario using `currentEvent.getDate()`
            String preservedDate = currentEvent.getDate(); // Fallback to eventDate if needed
            Event updatedEvent = createUpdatedEvent(eventID, preservedDate);

            // Update in Firestore
            database.updateEvent(updatedEvent, success -> {
                if (success) {
                    Toast.makeText(MoodCreateAndEditActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("newEvent", updatedEvent);
                    returnIntent.putExtra("selectedTab", "myposts");
                    setResult(RESULT_OK, returnIntent);
                    finish(); // Close and return
                } else {
                    Toast.makeText(MoodCreateAndEditActivity.this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Sets up a character count tracker and visual limit feedback for the explanation input.
     * - Updates the live count (e.g., "120/200")
     * - Turns the counter red when the limit is hit
     * - Prevents typing beyond 200 characters
     */
    private void setupTriggerExplanationWatcher() {
        triggerExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();

                explanationCounter.setText(Math.min(length, 200) + "/200");

                if (length >= 200) {
                    explanationCounter.setTextColor(
                            ContextCompat.getColor(MoodCreateAndEditActivity.this, android.R.color.holo_red_dark));
                } else {
                    explanationCounter.setTextColor(
                            ContextCompat.getColor(MoodCreateAndEditActivity.this, android.R.color.darker_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 200) {
                    String trimmed = s.subSequence(0, 200).toString();
                    triggerExplanation.setText(trimmed);
                    triggerExplanation.setSelection(trimmed.length());
                }
            }
        });
    }

    /**
     * Creates a new mood event and saves it to Firestore.
     * On success, navigates back to HomeActivity with the new event bundled.
     * @param database The database instance to handle event saving.
     */
    private void confirmCreateEvent(Database database) {

        // CREATE new Event scenario
        Event newEvent = createNewEvent();
        Log.d("MoodCreateAndEditActivity", "Navigating to HomeActivity with newEvent: " + newEvent.getTitle());

        // Save to Firestore first, then navigate back
        database.saveEventToFirebase(newEvent, success -> {
            if (success) {
                Log.d("MoodCreateAndEditActivity", "Event successfully saved to Firestore.");
                Intent intent = new Intent(MoodCreateAndEditActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("selectedTab", "myposts");
                intent.putExtra("newEvent", newEvent);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MoodCreateAndEditActivity.this, "Failed to save event.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Creates updated Event object with edited fields.
     * @param existingId Original event ID
     * @param eventDate Original event date
     * @return Updated Event object
     */
    Event createUpdatedEvent(String existingId, String eventDate) { // Accept date from caller

        String eventTitle = headerTitle.getText().toString().trim();

        int emojiResource = includeEmojiCheckbox.isChecked()
                ? updateEmojiIcon(selectedMood.getText().toString())
                : 0;
        boolean isFollowed = false;
        boolean isMyPost = true;
        String mood = selectedMood.getText().toString();
        String moodExplanation = triggerExplanation.getText().toString();
        String situation = selectedSituation.getText().toString();
        String finalImageUrl = (imageUrl != null) ? imageUrl : "";
        String overlayColor = getOverlayColorForMood(mood);
        User user = User.getInstance();
        return new Event(
                existingId, eventTitle, eventDate, overlayColor, finalImageUrl,
                emojiResource, isFollowed, isMyPost,
                mood, moodExplanation, situation, user.getUsername(),
                lat, lng, currentEvent.isPublic_status()
        );
    }


    /**
     * Validates explanation length.
     * @param explanation User's explanation text
     * @return true if valid (≤200 chars), false otherwise
     */
    boolean isValidExplanation(String explanation) {
        if (explanation.length() > 200) {
            return false;
        }
        return true;

    }

    /**
     * Create the Event object with all user inputs.
     */
    Event createNewEvent() {
        String eventID = UUID.randomUUID().toString();
        String eventTitle = headerTitle.getText().toString().trim();
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date());

        int emojiResource = includeEmojiCheckbox.isChecked()
                ? updateEmojiIcon(selectedMood.getText().toString())
                : 0;
        boolean isFollowed = false;
        boolean isMyPost = true;
        String mood = selectedMood.getText().toString();
        String moodExplanation = triggerExplanation.getText().toString();
        String situation = selectedSituation.getText().toString();
        String finalImageUrl = (imageUrl != null) ? imageUrl : "";
        String overlayColor = getOverlayColorForMood(mood);
        User user = User.getInstance();
        // Make sure your Event constructor includes lat & lng if you want to store them
        return new Event(
                eventID, eventTitle, eventDate, overlayColor, finalImageUrl,
                emojiResource, isFollowed, isMyPost,
                mood, moodExplanation, situation, user.getUsername(),
                lat, lng, postPublicStatus
        );
    }

    /**
     * Updates the emoji icon based on the chosen mood.
     */
    int updateEmojiIcon(String mood) {
        int emojiResId = EventManager.getEmojiResource(mood);
        emojiButton.setImageResource(emojiResId);
        return emojiResId;
    }

    /**
     * Picks a background color based on the user's selected mood.
     * Each emotion is tied to a unique hex color for visual feedback.
     * @param mood The emotion selected by the user (e.g., "anger", "happiness").
     * @return A hex color string that matches the mood (e.g., "#FF6347" for anger).
     */
    String getOverlayColorForMood(String mood) {
        return EventManager.getOverlayColorForMood(mood);
    }

    /**
     * Called after the user chooses whether their mood post should be public or private.
     * Once the choice is made, this triggers the creation and saving of the mood event.
     * @param post_status true if the post is public, false if it's private.
     */
    @Override
    public void public_status(boolean post_status) {
        postPublicStatus = post_status;

        Database database = new Database();
        confirmCreateEvent(database);
    }
}
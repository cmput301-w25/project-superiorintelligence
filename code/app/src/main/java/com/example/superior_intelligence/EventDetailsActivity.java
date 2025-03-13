/**
 * This class shows details of the mood event when the user interact with the event.
 * Contains button to edit mood
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventMood, selectedMood, eventReason, eventSituation, eventDate, eventUser;
    private String title, mood, reason, situation, overlayColor, date, user;

    private ActivityResultLauncher<Intent> editEventLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        ImageView eventImage = findViewById(R.id.event_full_image);
        eventTitle = findViewById(R.id.event_detail_title);
        eventMood = findViewById(R.id.event_detail_mood); // "Mood: "
        selectedMood = findViewById(R.id.selected_mood);  // Dynamic mood
        eventReason = findViewById(R.id.event_detail_reason);
        eventSituation = findViewById(R.id.event_detail_situation);
        eventDate = findViewById(R.id.event_detail_date);
        eventUser = findViewById(R.id.event_detail_user);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton editButton = findViewById(R.id.editButton);

        // Retrieve event data
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        mood = intent.getStringExtra("mood");
        reason = intent.getStringExtra("reason");
        situation = intent.getStringExtra("situation");
        overlayColor = intent.getStringExtra("overlayColor");
        date = intent.getStringExtra("date");
        user = intent.getStringExtra("user");

        // Set initial values
        setEventDetails();

        // Initialize launcher for editing
        initEditLauncher();

        // Back button
        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(EventDetailsActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
        });

        // Edit button
        editButton.setOnClickListener(view -> {
            Intent editIntent = new Intent(EventDetailsActivity.this, MoodCreateAndEditActivity.class);
            editIntent.putExtra("title", title);
            editIntent.putExtra("mood", mood);
            editIntent.putExtra("reason", reason);
            editIntent.putExtra("socialSituation", situation);
            editIntent.putExtra("overlayColor", overlayColor);
            editEventLauncher.launch(editIntent); // Launch edit and wait for result
        });
    }

    /**
     * initEditLauncher function retrieve result from the mood event user clicked on
     */
    private void initEditLauncher() {
        editEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get updated details
                        title = result.getData().getStringExtra("title");
                        mood = result.getData().getStringExtra("mood");
                        reason = result.getData().getStringExtra("reason");
                        situation = result.getData().getStringExtra("socialSituation");
                        overlayColor = result.getData().getStringExtra("overlayColor");

                        // Refresh display
                        setEventDetails();
                    }
                }
        );
    }

    /**
     * setEventDetails function set the retrieved details of moodEvent on the screen
     */
    private void setEventDetails() {
        eventTitle.setText(title);
        eventMood.setText("Mood: ");
        selectedMood.setText(mood);

        // Set color dynamically
        if (overlayColor != null && !overlayColor.isEmpty()) {
            GradientDrawable bgShape = (GradientDrawable) selectedMood.getBackground().mutate(); // Ensure mutable instance
            bgShape.setColor(Color.parseColor(overlayColor));
        }

        // Ensure non-null values
        eventReason.setText("Reason: " + (reason != null ? reason : "No reason provided"));
        if (situation.equals("Select a Situation")) {
            eventSituation.setText("Social Situation: No situation provided");
        } else {
            eventSituation.setText("Social Situation: " + (situation != null ? situation : "No situation provided"));
        }
        eventDate.setText("Date: " + (date != null ? date : "Unknown Date"));
        eventUser.setText("Posted by: " + (user != null ? user : "Anonymous"));
    }
}

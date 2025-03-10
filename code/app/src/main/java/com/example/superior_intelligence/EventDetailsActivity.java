package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
//import com.bumptech.glide.Glide;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        ImageView eventImage = findViewById(R.id.event_full_image);
        TextView eventTitle = findViewById(R.id.event_detail_title);
        TextView eventMood = findViewById(R.id.event_detail_mood); // This will show "Mood: "
        TextView selectedMood = findViewById(R.id.selected_mood); // This will show the actual mood word with color
        TextView eventReason = findViewById(R.id.event_detail_reason);
        TextView eventSituation = findViewById(R.id.event_detail_situation);
        ImageButton backButton = findViewById(R.id.back_button); // Find back button

        // Retrieve event data
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String mood = intent.getStringExtra("mood");
        String reason = intent.getStringExtra("reason");
        String situation = intent.getStringExtra("situation");
        String imageUrl = intent.getStringExtra("imageUrl");
        String overlayColor = intent.getStringExtra("overlayColor"); // Fetch mood color

        // Set title
        eventTitle.setText(title);

        // Set Mood label
        eventMood.setText("Mood: "); // Static label

        // Set Mood value (actual mood word)
        selectedMood.setText(mood); // Dynamic word like "Excited", "Fear", etc.

        // Set dynamic background color for mood
        if (overlayColor != null && !overlayColor.isEmpty()) {
            GradientDrawable bgShape = (GradientDrawable) selectedMood.getBackground();
            bgShape.setColor(Color.parseColor(overlayColor)); // Set color on drawable dynamically
        }

        // Set Reason (or empty if not provided)
        if (reason != null && !reason.isEmpty()) {
            eventReason.setText("Reason: " + reason);
        } else {
            eventReason.setText("Reason: ");
        }

        // Set Social Situation (or empty if not provided or default "Select a Situation")
        if (situation != null && !situation.isEmpty() && !situation.equals("Select a Situation")) {
            eventSituation.setText("Social Situation: " + situation);
        } else {
            eventSituation.setText("Social Situation: ");
        }

        // Load Image if available (optional future use)
        // if (imageUrl != null && !imageUrl.isEmpty()) {
        //     Glide.with(this).load(Uri.parse(imageUrl)).into(eventImage);
        // }

        // Back button navigates to Home Page
        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(EventDetailsActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clears the activity stack
            startActivity(homeIntent);
            finish();
        });
    }
}

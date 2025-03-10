package com.example.superior_intelligence;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
//import com.bumptech.glide.Glide;

/**
 * This class shows a mood event details when a mood event is clicked on.
 * Contains an edit button to go edit mood details
 * Contains a back button to go back to home page
 */
public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        ImageView eventImage = findViewById(R.id.event_full_image);
        TextView eventTitle = findViewById(R.id.event_detail_title);
        TextView eventMood = findViewById(R.id.event_detail_mood);
        TextView eventReason = findViewById(R.id.event_detail_reason);
        TextView eventSituation = findViewById(R.id.event_detail_situation);
        ImageButton backButton = findViewById(R.id.back_button); // Find back button
        ImageButton editButton = findViewById(R.id.editButton);

        // Retrieve event data
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String mood = intent.getStringExtra("mood");
        String reason = intent.getStringExtra("reason");
        String situation = intent.getStringExtra("situation");
        String imageUrl = intent.getStringExtra("imageUrl");

        // Set data to views
        eventTitle.setText(title);
        eventMood.setText("Mood: " + mood);
        eventReason.setText("Reason: " + reason);
        eventSituation.setText("Social Situation: " + situation);

        // Load Image if available
        //if (imageUrl != null && !imageUrl.isEmpty()) {
            //Glide.with(this).load(Uri.parse(imageUrl)).into(eventImage);
        //}

        // Back button navigates to Home Page
        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(EventDetailsActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clears the activity stack
            startActivity(homeIntent);
            finish();
        });

        //Mood oldMood = new Mood(title, mood, reason, situation);



        // Edit button navigates to Mood Create/Edit Page
        editButton.setOnClickListener(view -> {
            Intent editIntent = new Intent(EventDetailsActivity.this, MoodCreateAndEditActivity.class);
            editIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            editIntent.putExtra("title", title);
            editIntent.putExtra("mood", mood);
            editIntent.putExtra("reason", reason);
            editIntent.putExtra("socialSituation", situation);
            startActivity(editIntent);
            finish();
        });

    }

}

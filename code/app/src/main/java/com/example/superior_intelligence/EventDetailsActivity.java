/**
 * This class shows details of the mood event when the user interact with the event.
 * Contains button to edit mood
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventMood, selectedMood, eventReason, eventSituation, eventDate, eventUser;
    private String id, title, mood, reason, situation, overlayColor, date, user, imageDocID, currentUser;

    private TextView noCommentsText;
    private EditText commentInput;
    private RecyclerView commentsRecyclerView;
    private  ImageView eventImage;
    private ActivityResultLauncher<Intent> editEventLauncher;
    private boolean isMyPost;
    private List<String> commentsList;
    private ImageButton sendCommentButton, backButton;
    private CardView profileCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        currentUser = User.getInstance().getUsername();
        eventImage = findViewById(R.id.event_full_image);
        eventTitle = findViewById(R.id.event_detail_title);
        eventMood = findViewById(R.id.event_detail_mood); // "Mood: "
        selectedMood = findViewById(R.id.selected_mood);  // Dynamic mood
        eventReason = findViewById(R.id.event_detail_reason);
        eventSituation = findViewById(R.id.event_detail_situation);
        eventDate = findViewById(R.id.event_detail_date);
        eventUser = findViewById(R.id.event_detail_user);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        noCommentsText = findViewById(R.id.no_comments_text);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton editButton = findViewById(R.id.editButton);

        // Retrieve event data
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        mood = intent.getStringExtra("mood");
        reason = intent.getStringExtra("reason");
        situation = intent.getStringExtra("situation");
        overlayColor = intent.getStringExtra("overlayColor");
        date = intent.getStringExtra("date");
        user = intent.getStringExtra("user");
        imageDocID = intent.getStringExtra("imageUrl");
        commentsList = intent.getStringArrayListExtra("comments");
        isMyPost = currentUser != null && currentUser.equals(user);

        // Ensure the comments list is never null
        if (commentsList == null) {
            commentsList = new ArrayList<>();
        }

        // Load different layouts based on event ownership
        if (isMyPost) {
            setContentView(R.layout.event_details); // User's own post
        } else {
            setContentView(R.layout.others_event_details); // Another user's post
        }

        // Set initial values
        initializeUI();
        setEventDetails();
        setupCommentsSection();

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
            editIntent.putExtra("id", id); //Pass event ID for proper update tracking
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
                        id = result.getData().getStringExtra("id"); // Retrieve ID again to ensure it's the same
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

        // Handle the image display logic
        if (imageDocID != null && !imageDocID.isEmpty()) {
            Photobase photobase = new Photobase(this);
            photobase.loadImage(imageDocID, new Photobase.ImageLoadCallback() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    eventImage.setVisibility(View.VISIBLE); // Show the image if it exists
                    eventImage.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadFailed(String error) {
                    eventImage.setVisibility(View.GONE); // Hide ImageView if loading fails
                    Log.e("EventDetailsActivity", "Failed to load event image: " + error);
                }
            });
        } else {
            eventImage.setVisibility(View.GONE); // Hide ImageView if no image exists
        }
    }
    /**
     * Handles displaying comments section
     */
    private void setupCommentsSection() {
        if (commentsList == null || commentsList.isEmpty()) {
            commentsRecyclerView.setVisibility(View.GONE);
            noCommentsText.setVisibility(View.VISIBLE); // Show "No Comments Yet"
        } else {
            noCommentsText.setVisibility(View.GONE); // Hide placeholder
            commentsRecyclerView.setVisibility(View.VISIBLE);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            commentsRecyclerView.setAdapter(new CommentsAdapter(commentsList));
        }
    }

    private void initializeUI() {
        // Common Views
        eventImage = findViewById(R.id.event_full_image);
        eventTitle = findViewById(R.id.event_detail_title);
        eventMood = findViewById(R.id.event_detail_mood);
        selectedMood = findViewById(R.id.selected_mood);
        eventReason = findViewById(R.id.event_detail_reason);
        eventSituation = findViewById(R.id.event_detail_situation);
        eventDate = findViewById(R.id.event_detail_date);
        eventUser = findViewById(R.id.event_detail_user);
        backButton = findViewById(R.id.back_button);
        noCommentsText = findViewById(R.id.no_comments_text);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        commentsList = getIntent().getStringArrayListExtra("comments");
        profileCard = findViewById(R.id.profile_image);

        if (isMyPost) {
            // Views for user's own post
            ImageButton editButton = findViewById(R.id.editButton);
            editButton.setOnClickListener(view -> {
                Intent editIntent = new Intent(EventDetailsActivity.this, MoodCreateAndEditActivity.class);
                editIntent.putExtra("title", title);
                editIntent.putExtra("mood", mood);
                editIntent.putExtra("reason", reason);
                editIntent.putExtra("socialSituation", situation);
                editIntent.putExtra("overlayColor", overlayColor);
                editEventLauncher.launch(editIntent);
            });
        } else {
            // Views for another user's post

            profileCard.setOnClickListener(view -> {
                if (user != null) {
                    Intent profileIntent = new Intent(EventDetailsActivity.this, OtherUserProfileActivity.class);
                    profileIntent.putExtra("username", user);
                    startActivity(profileIntent);
                } else {
                    Log.e("EventDetailsActivity", "User not found, cannot open profile.");
                }
            });

            commentInput = findViewById(R.id.comment_input);
            sendCommentButton = findViewById(R.id.send_comment_button);

            sendCommentButton.setOnClickListener(view -> {
                String newComment = commentInput.getText().toString().trim();
                if (!newComment.isEmpty()) {
                    commentsList.add(newComment);
                    commentInput.setText("");
                    setupCommentsSection();
                }
            });
        }
        backButton.setOnClickListener(v -> {
            if (!isMyPost) {
                // If in another user's event, go back to HomeActivity
                Intent homeIntent = new Intent(EventDetailsActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish();
            } else {
                finish();
            }
        });
    }
}

package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity implements DeleteMoodFragment.DeleteDialogListener{

    private TextView eventTitle, eventMood, selectedMood, eventReason, eventSituation, eventDate, eventUser;
    private TextView noCommentsText;
    private EditText commentInput;
    private RecyclerView commentsRecyclerView;
    private ImageView eventImage;
    private ImageButton sendCommentButton, backButton;
    private CardView profileCard;

    private ActivityResultLauncher<Intent> editEventLauncher;
    private ActivityResultLauncher<Intent> deleteEventLauncher;

    private boolean isMyPost;
    private Event currentEvent;
    private List<String> commentsList;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = User.getInstance().getUsername();

        // Retrieve entire Event object
        currentEvent = (Event) getIntent().getSerializableExtra("event");

        if (currentEvent == null) {
            Log.e("EventDetailsActivity", "No event data passed!");
            finish();
            return;
        }

        isMyPost = currentUser != null && currentUser.equals(currentEvent.getUser());
        commentsList = currentEvent.getComments() != null ? currentEvent.getComments() : new ArrayList<>();

        // Load appropriate layout
        setContentView(isMyPost ? R.layout.event_details : R.layout.others_event_details);

        // Initialize UI elements
        initializeUI();
        setEventDetails();
        setupCommentsSection();
        initEditLauncher(); // Set up editing capability

        // Back button handler
        backButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("selectedTab", "myposts");
            // Do NOT send "newEvent" unless it was edited
            setResult(RESULT_OK, returnIntent);
            finish();
        });

        // Edit button (only for own posts)
        if (isMyPost) {
            ImageButton editButton = findViewById(R.id.editButton);
            editButton.setOnClickListener(view -> {
                Intent editIntent = new Intent(EventDetailsActivity.this, MoodCreateAndEditActivity.class);
                editIntent.putExtra("event", currentEvent); // Pass whole Event object
                editEventLauncher.launch(editIntent);
            });


            Button deleteButton = findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(view -> {;
                // delete currId from database
                DeleteMoodFragment deleteDialogFragment = new DeleteMoodFragment();
                deleteDialogFragment.show(getSupportFragmentManager(), "DeleteMoodDialog");
            });
        }
    }


    /**
     * Initializes the launcher to handle edit results.
     */
    private void initEditLauncher() {
        editEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Event updatedEvent = (Event) result.getData().getSerializableExtra("newEvent");
                        if (updatedEvent != null) {
                            // Return to HomeActivity with updated event
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("newEvent", updatedEvent);
                            returnIntent.putExtra("selectedTab", "myposts");
                            setResult(RESULT_OK, returnIntent);
                            finish(); // Close details activity
                        }
                    }
                }
        );
    }

    /**
     * Sets event details into UI.
     */
    private void setEventDetails() {
        eventTitle.setText(currentEvent.getTitle());
        eventMood.setText("Mood: ");
        selectedMood.setText(currentEvent.getMood());

        // Set color dynamically
        String overlayColor = currentEvent.getOverlayColor();
        if (overlayColor != null && !overlayColor.isEmpty()) {
            GradientDrawable bgShape = (GradientDrawable) selectedMood.getBackground().mutate();
            bgShape.setColor(Color.parseColor(overlayColor));
        }

        eventReason.setText("Reason: " + (currentEvent.getMoodExplanation() != null ? currentEvent.getMoodExplanation() : "No reason provided"));
        String situation = currentEvent.getSituation();
        if ("Select a Situation".equals(situation)) {
            eventSituation.setText("Social Situation: No situation provided");
        } else {
            eventSituation.setText("Social Situation: " + (situation != null ? situation : "No situation provided"));
        }
        eventDate.setText("Date: " + (currentEvent.getDate() != null ? currentEvent.getDate() : "Unknown Date"));
        eventUser.setText("Posted by: " + (currentEvent.getUser() != null ? currentEvent.getUser() : "Anonymous"));

        // Handle image loading
        String imageDocID = currentEvent.getImageUrl();
        if (imageDocID != null && !imageDocID.isEmpty()) {
            Photobase photobase = new Photobase(this);
            photobase.loadImage(imageDocID, new Photobase.ImageLoadCallback() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    eventImage.setVisibility(View.VISIBLE);
                    eventImage.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadFailed(String error) {
                    eventImage.setVisibility(View.GONE);
                    Log.e("EventDetailsActivity", "Failed to load event image: " + error);
                }
            });
        } else {
            eventImage.setVisibility(View.GONE);
        }
    }

    /**
     * Initializes and sets up comments section.
     */
    private void setupCommentsSection() {
        if (commentsList.isEmpty()) {
            commentsRecyclerView.setVisibility(View.GONE);
            noCommentsText.setVisibility(View.VISIBLE);
        } else {
            noCommentsText.setVisibility(View.GONE);
            commentsRecyclerView.setVisibility(View.VISIBLE);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            commentsRecyclerView.setAdapter(new CommentsAdapter(commentsList));
        }
    }

    /**
     * Initializes common UI elements.
     */
    private void initializeUI() {
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
        profileCard = findViewById(R.id.profile_image);

        // For others' posts, enable commenting
        if (!isMyPost) {
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

            profileCard.setOnClickListener(view -> {
                if (currentEvent.getUser() != null) {
                    Intent profileIntent = new Intent(EventDetailsActivity.this, OtherUserProfileActivity.class);
                    profileIntent.putExtra("username", currentEvent.getUser());
                    startActivity(profileIntent);
                }
            });
        }
    }

    /**
     * Delete mood from database when user click confirm delete
     */
    @Override
    public void delete(boolean delete_status) {
        if (delete_status){
            Database db = new Database();
            db.deleteEvent(currentEvent.getID());
        }
        finish();

    }
}

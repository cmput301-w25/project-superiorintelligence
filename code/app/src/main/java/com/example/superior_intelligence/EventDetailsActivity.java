package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EventDetailsActivity extends AppCompatActivity implements DeleteMoodFragment.DeleteDialogListener, PostStatusFragment.PostStatusDialogListener{

    private TextView eventTitle, eventMood, selectedMood, eventReason, eventSituation, eventDate, eventUser;
    private TextView noCommentsText;
    private EditText commentInput;
    private RecyclerView commentsRecyclerView;
    private ImageView eventImage, publicEmo, privateEmo;
    private ImageButton sendCommentButton, backButton;
    private CardView profileCard;

    private ActivityResultLauncher<Intent> editEventLauncher;
    private ActivityResultLauncher<Intent> deleteEventLauncher;

    private boolean isMyPost;
    private Event currentEvent;
    private List<Comment> commentsList = new ArrayList<>();
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("EventDetailsActivity", "onCreate triggered");
        currentUser = User.getInstance().getUsername();
        Log.d("EventDetailsActivity", "Current logged-in user: " + currentUser);
        // Retrieve entire Event object
        currentEvent = (Event) getIntent().getSerializableExtra("event");

        if (currentEvent == null) {
            Log.e("EventDetailsActivity", "No event data passed!");
            finish();
            return;
        }

        Log.d("EventDetailsActivity", "Event loaded: " + currentEvent.getTitle());
        Log.d("EventDetailsActivity", "Event posted by: " + currentEvent.getUser());

        isMyPost = currentUser != null && currentUser.equals(currentEvent.getUser());

        // Load appropriate layout
        setContentView(isMyPost ? R.layout.event_details : R.layout.others_event_details);

        // Initialize UI elements
        initializeUI();
        setEventDetails();
        initEditLauncher(); // Set up editing capability

        loadCommentsFromFirestore(currentEvent.getID());

        // Back button handler
        backButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            if (isMyPost) {
                returnIntent.putExtra("selectedTab", "myposts");
            } else {
                returnIntent.putExtra("selectedTab", "explore");
            }
            returnIntent.putExtra("textFilter", getIntent().getStringExtra("textFilter")); // Preserve filter
            setResult(RESULT_OK, returnIntent);
            finish();
        });

        // Edit/Delete button (only for own posts)
        if (isMyPost) {
            Button editButton = findViewById(R.id.edit_button);
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


            ImageView editStatButton;
            if (publicEmo.getVisibility() == View.VISIBLE){
                editStatButton = publicEmo;
            } else {
                editStatButton = privateEmo;
            }
            editStatButton.setOnClickListener(view -> {
                // public status fragment
                PostStatusFragment publicDialogFragment = new PostStatusFragment(currentEvent);
                publicDialogFragment.show(getSupportFragmentManager(), "PostStatusDialog");
            });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("selectedTab", "myposts");
                        returnIntent.putExtra("textFilter", getIntent().getStringExtra("textFilter")); // Preserve filter
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
            );
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("selectedTab", "myposts");
        returnIntent.putExtra("textFilter", getIntent().getStringExtra("textFilter")); // Preserve filter
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed(); // Finish the activity normally
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

        if (isMyPost) {
            // Set public status emoticon
            if (currentEvent.isPublic_status()) {
                publicEmo.setVisibility(View.VISIBLE);
            } else {
                privateEmo.setVisibility(View.VISIBLE);
            }
        }

        Log.d("EventDetails", "Loading event by user: " + currentEvent.getUser());
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
                String commentText = commentInput.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    String username = User.getInstance().getUsername();
                    String timestamp = new SimpleDateFormat("dd/MM/yy H:mm", Locale.getDefault()).format(new Date());
                    Comment newComment = new Comment(username, commentText, timestamp);
                    commentsList.add(newComment);

                    // Refresh UI
                    setupCommentsSection();
                    // Clear text input
                    commentInput.setText("");
                    Database.getInstance().saveCommentToEvent(currentEvent.getID(), newComment);
                }
            });

            profileCard.setOnClickListener(view -> {
                String eventUsername = currentEvent.getUser();
                Log.d("EventDetailsActivity", "Attempting to open profile for user: " + eventUsername);
                if (currentEvent.getUser() != null) {
                    Intent profileIntent = new Intent(EventDetailsActivity.this, OtherUserProfileActivity.class);
                    profileIntent.putExtra("username", currentEvent.getUser());
                    startActivity(profileIntent);
                }
                else {
                    Log.e("EventDetailsActivity", "currentEvent.getUser() is null or empty");
                }
            });
        } else {
            publicEmo = findViewById(R.id.public_status_detail);
            privateEmo = findViewById(R.id.private_status_detail);
        }
    }

    private void loadCommentsFromFirestore(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("MyPosts").document(postId);

        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("comments")) {
                // Safely fill commentsList (already != null)
                commentsList.clear();
                Map<String, List<Map<String, String>>> commentsMap =
                        (Map<String, List<Map<String, String>>>) documentSnapshot.get("comments");

                for (String user : commentsMap.keySet()) {
                    List<Map<String, String>> userComments = commentsMap.get(user);
                    for (Map<String, String> commentData : userComments) {
                        commentsList.add(new Comment(
                                user,
                                commentData.get("text"),
                                commentData.get("date")
                        ));
                    }
                }
            } else {
                // If no "comments" field, or doc not found, your list remains empty
                commentsList.clear();
            }

            // Now that commentsList is updated, show them
            setupCommentsSection();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error loading comments", e);
            // Optionally handle error (list remains empty)
            setupCommentsSection();
        });
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

    @Override
    public void public_status(boolean post_status) {
        if (currentEvent.isPublic_status() != post_status){
            currentEvent.setPublic_status(post_status);

            Database database = new Database();
            // Update in Firestore
            database.updateEvent(currentEvent, success -> {
                if (success) {
                    Toast.makeText(EventDetailsActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("newEvent", currentEvent);
                    returnIntent.putExtra("selectedTab", "myposts");
                    setResult(RESULT_OK, returnIntent);
                    finish(); // Close and return
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}

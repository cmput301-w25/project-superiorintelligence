package com.example.superior_intelligence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.EventListener;

import android.util.Log;
import androidx.annotation.Nullable;

/**
 *
 */
public class HomeActivity extends AppCompatActivity implements EventAdapter.OnFollowToggleListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;

    // Separate lists for each tab
    private List<Event> exploreEvents = new ArrayList<>();
    private List<Event> followedEvents = new ArrayList<>();
    private List<Event> myPostsEvents = new ArrayList<>();

    private TextView tabExplore, tabFollowed, tabMyPosts;
    FirebaseFirestore db;
    CollectionReference myPostsRef;
    /**
     * Initializes the activity, sets up Firestore, event lists, and UI elements.
     * @param savedInstanceState instance of activity on start up
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        db = FirebaseFirestore.getInstance();
        myPostsRef = db.collection("MyPosts");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tab UI elements
        tabExplore = findViewById(R.id.tab_explore);
        tabFollowed = findViewById(R.id.tab_followed);
        tabMyPosts = findViewById(R.id.tab_myposts);

        // Load sample events into the lists (For now until we can add our own)
        loadEventsFromFirebase();

        // Creates an adapter, default to Explore list
        // Passes all 3 lists to the adapter for follow/unfollow logic
        adapter = new EventAdapter(exploreEvents, followedEvents, myPostsEvents, this);
        recyclerView.setAdapter(adapter);

        Event newEvent = (Event) getIntent().getSerializableExtra("newEvent");

        if (newEvent != null && newEvent.isMyPost()) {
            if (!myPostsEvents.contains(newEvent)) {
                Log.d("Firebase Debug", "Adding to MyPosts: " + newEvent.getTitle());
                myPostsEvents.add(newEvent);
                adapter.setEvents(myPostsEvents);
                saveEventToFirebase(newEvent);
            }
            adapter.notifyDataSetChanged();
        }
        if (newEvent != null && !newEvent.isMyPost()) {
            Log.d("Firebase Debug", "Adding to Explore: " + newEvent.getTitle());
            exploreEvents.add(newEvent);
        }

        String selectedTab = getIntent().getStringExtra("selectedTab");
        if ("myposts".equals(selectedTab)) {
            switchTab(myPostsEvents, tabMyPosts);
        } else {
            switchTab(exploreEvents, tabExplore); // Ensure only Explore is bolded at start
        }

        // Set tab listeners
        tabExplore.setOnClickListener(v -> switchTab(exploreEvents, tabExplore));
        tabFollowed.setOnClickListener(v -> switchTab(followedEvents, tabFollowed));
        tabMyPosts.setOnClickListener(v -> switchTab(myPostsEvents, tabMyPosts));

        // Launch's MoodCreateAndEditActivity when clicked
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodCreateAndEditActivity.class);
            startActivity(intent);
        });

        // Launch's ProfileActivity when clicked
        CardView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Makes sure the RecyclerView updates when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); // Ensure RecyclerView updates when returning
    }

    /**
     * Saves a new event to Firestore under the "MyPosts" collection.
     * @param event The event object to be stored in Firestore.
     */
    void saveEventToFirebase(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference myPostsRef = db.collection("MyPosts");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", event.getTitle());
        eventData.put("date", event.getDate()); // Ensures date is saved as a String
        eventData.put("overlayColor", event.getOverlayColor());
        eventData.put("imageUrl", event.getImageUrl());
        eventData.put("emojiResource", event.getEmojiResource());
        eventData.put("isFollowed", event.isFollowed());
        eventData.put("isMyPost", event.isMyPost());
        eventData.put("mood", event.getMood());
        eventData.put("situation", event.getSituation());
        eventData.put("moodExplanation", event.getMoodExplanation());
        eventData.put("postUser", event.getUser());

        myPostsRef.add(eventData)
                .addOnSuccessListener(documentReference -> Log.d("Firebase", "Event saved: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firebase", "Error saving event", e));
    }
    /**
     * Loads events from Firestore under the "MyPosts" collection and updates the UI.
     */
    void loadEventsFromFirebase() {
        myPostsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myPostsEvents.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Extract values safely
                    String title = document.getString("title");

                    // Convert date from Firestore (Handles Timestamp or String)
                    Object rawDate = document.get("date");
                    String date = "Unknown Date";
                    if (rawDate instanceof String) {
                        date = (String) rawDate; // Already stored as a String
                    } else if (rawDate instanceof com.google.firebase.Timestamp) {
                        date = new java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                .format(((com.google.firebase.Timestamp) rawDate).toDate()); // Convert Timestamp
                    }

                    String overlayColor = document.getString("overlayColor");
                    String imageUrl = document.getString("imageUrl");
                    int emojiResource = document.contains("emojiResource") ? document.getLong("emojiResource").intValue() : 0;
                    boolean isFollowed = document.contains("isFollowed") && Boolean.TRUE.equals(document.getBoolean("isFollowed"));
                    boolean isMyPost = document.contains("isMyPost") && Boolean.TRUE.equals(document.getBoolean("isMyPost"));
                    String mood = document.getString("mood");
                    String situation = document.getString("situation");
                    String moodExplanation = document.getString("moodExplanation");
                    String user = document.getString("user");

                    // Create event object
                    Event event = new Event(title, date, overlayColor, imageUrl, emojiResource, isFollowed, isMyPost, mood, moodExplanation, situation, user);

                    myPostsEvents.add(event); // Add event to list
                }

                // Ensure MyPosts only loads if it was selected
                String selectedTab = getIntent().getStringExtra("selectedTab");
                if ("myposts".equals(selectedTab)) {
                    switchTab(myPostsEvents, tabMyPosts); // Show MyPosts correctly
                }

                adapter.notifyDataSetChanged(); // Refresh RecyclerView
            } else {
                Log.w("Firebase", "Error getting documents.", task.getException());
            }
        });
    }

    /**
     * Switches between Explore, Followed, and MyPosts tabs, updating the UI accordingly.
     *
     * @param targetList  The list of events to display.
     * @param selectedTab The selected tab's TextView to apply bold styling.
     */
    private void switchTab(List<Event> targetList, TextView selectedTab) {
        // Reset all tabs to normal style
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Bold only the selected tab
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        adapter.setEvents(targetList);
        adapter.notifyDataSetChanged();

    }

    /**
     * Handles follow/unfollow toggle events.
     *
     * @param event     The event object being followed/unfollowed.
     * @param isFollowed True if the event is now followed, false otherwise.
     */
    @Override
    public void onFollowToggled(Event event, boolean isFollowed) {
        // Will be helpful for whoever works on logic (e.g., Toasts, DB saving).

    }
}

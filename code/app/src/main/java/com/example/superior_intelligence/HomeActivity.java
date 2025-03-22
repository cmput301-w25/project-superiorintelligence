package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private Database database;

    // Separate lists for each tab
    private List<Event> exploreEvents = new ArrayList<>();
    private List<Event> followedEvents = new ArrayList<>();
    private List<Event> myPostsEvents = new ArrayList<>();

    private TextView tabExplore, tabFollowed, tabMyPosts, tabMap;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("AuthDebug", "No user session found! Redirecting to login.");
            Intent loginIntent = new Intent(this, LoginPageActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish(); // Stop execution of HomeActivity if user is not logged in
        } else {
            Log.d("AuthDebug", "User logged in: " + currentUser.getEmail());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        database = new Database();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        recyclerView.setAdapter(adapter);

        // Tab UI elements
        tabExplore = findViewById(R.id.tab_explore);
        tabFollowed = findViewById(R.id.tab_followed);
        tabMyPosts = findViewById(R.id.tab_myposts);
        tabMap = findViewById(R.id.tab_map);

        // Set tab listeners (do this ONCE, not inside switchTab!)
        tabExplore.setOnClickListener(v -> switchTab(exploreEvents, tabExplore));
        tabFollowed.setOnClickListener(v -> switchTab(followedEvents, tabFollowed));
        tabMyPosts.setOnClickListener(v -> switchTab(myPostsEvents, tabMyPosts));
        tabMap.setOnClickListener(v -> {
            switchTab(new ArrayList<>(), tabMap);
            startActivity(new Intent(HomeActivity.this, MoodMap.class));
        });

        // Add new event
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MoodCreateAndEditActivity.class)));

        // Profile and notification buttons
        CardView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        ImageButton notificationButton = findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        // Load events and handle any incoming event
        loadAllEvents();
        handleIncomingEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllEvents();
    }

    /**
     * Handle incoming event to add or update.
     */
    private void handleIncomingEvent() {
        Event newEvent = (Event) getIntent().getSerializableExtra("newEvent");
        if (newEvent != null && newEvent.isMyPost()) {
            boolean found = false;
            for (int i = 0; i < myPostsEvents.size(); i++) {
                if (myPostsEvents.get(i).getID().equals(newEvent.getID())) {
                    myPostsEvents.set(i, newEvent); // Update existing
                    found = true;
                    break;
                }
            }
            if (!found) {
                myPostsEvents.add(newEvent); // Add if new
            }
        }

        // Select correct tab after handling event
        String selectedTab = getIntent().getStringExtra("selectedTab");
        if ("myposts".equals(selectedTab)) {
            switchTab(myPostsEvents, tabMyPosts);
        } else if ("followed".equals(selectedTab)) {
            switchTab(followedEvents, tabFollowed);
        } else {
            switchTab(exploreEvents, tabExplore);
        }
    }

    /**
     * Load events from Firestore and update UI.
     */
    private void loadAllEvents() {
        User currentUser = User.getInstance();

        if (currentUser == null) {
            Log.e("UserDebug", "User instance is NULL! Skipping event loading.");
            return; // Prevent crash
        }

        database.loadEventsFromFirebase(currentUser, (myPosts, explore, followed) -> {
            if (myPosts != null && explore != null && followed != null) {
                myPostsEvents.clear();
                exploreEvents.clear();
                followedEvents.clear();

                myPostsEvents.addAll(myPosts);
                exploreEvents.addAll(explore);
                followedEvents.addAll(followed);

                // Default to showing MyPosts tab if user is returning
                switchTab(myPostsEvents, tabMyPosts);

                adapter.notifyDataSetChanged();
            } else {
                Log.w("HomeActivity", "Failed to load events from Firestore");
            }
        });
    }

    /**
     * Switch tabs and update UI.
     */
    private void switchTab(List<Event> targetList, TextView selectedTab) {
        // Reset styles for all tabs
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Highlight selected tab
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        // Set events to adapter and refresh RecyclerView (no need to reset adapter)
        adapter.setEvents(targetList);
    }

    /**
     * Saves a new event to Firestore under the "MyPosts" collection.
     */
    private void saveEventToFirebase(Event event) {
        database.saveEventToFirebase(event, success -> {
            if (success) {
                Log.d("HomeActivity", "Event saved successfully");
            } else {
                Log.e("HomeActivity", "Error saving event");
            }
        });
    }

    /**
     * Update event in Firestore using Database.java
     */
    private void updateEventInFirebase(Event event) {
        database.updateEvent(event, success -> {
            if (success) {
                Log.d("HomeActivity", "Event updated successfully");
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("HomeActivity", "Error updating event");
                Toast.makeText(this, "Failed to update event!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

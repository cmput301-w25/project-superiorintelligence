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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

public class HomeActivity extends AppCompatActivity implements EventAdapter.OnFollowToggleListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;

    // Separate lists for each tab
    private List<Event> exploreEvents = new ArrayList<>();
    private List<Event> followedEvents = new ArrayList<>();
    private List<Event> myPostsEvents = new ArrayList<>();

    private TextView tabExplore, tabFollowed, tabMyPosts;
    private FirebaseFirestore db;
    private CollectionReference myPostsRef;

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

        // Load existing events
        loadSampleEvents();
        loadEventsFromFirebase();

        // Initialize adapter with empty lists
        adapter = new EventAdapter(exploreEvents, followedEvents, myPostsEvents, this);
        recyclerView.setAdapter(adapter);

        // Get event data from intent
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

        // Ensure the correct tab is selected after posting
        String selectedTab = getIntent().getStringExtra("selectedTab");
        if ("myposts".equals(selectedTab)) {
            switchTab(myPostsEvents, tabMyPosts);
        } else {
            switchTab(exploreEvents, tabExplore);
        }

        // Ensure tabs are always clickable, even if no events exist
        enableTabs();

        // Set tab listeners
        tabExplore.setOnClickListener(v -> switchTab(exploreEvents, tabExplore));
        tabFollowed.setOnClickListener(v -> switchTab(followedEvents, tabFollowed));
        tabMyPosts.setOnClickListener(v -> switchTab(myPostsEvents, tabMyPosts));

        // Launch MoodCreateAndEditActivity when clicking the add button
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodCreateAndEditActivity.class);
            startActivity(intent);
        });

        // Launch ProfileActivity when clicking the profile image
        CardView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Refresh events when returning to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    /**
     * Enables tab clicks at all times.
     */
    private void enableTabs() {
        tabExplore.setClickable(true);
        tabFollowed.setClickable(true);
        tabMyPosts.setClickable(true);
    }

    /**
     * Converts a date string (e.g., "15 Apr 2019") to a Date object.
     */
    private Date convertToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Default to current date if parsing fails
        }
    }

    /**
     * Saves the new event to Firebase Firestore.
     */
    private void saveEventToFirebase(Event event) {
        myPostsRef.add(event)
                .addOnSuccessListener(documentReference -> Log.d("Firebase", "Event saved: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firebase", "Error saving event", e));
    }

    /**
     * Loads saved events from Firebase Firestore.
     */
    private void loadEventsFromFirebase() {
        myPostsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myPostsEvents.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    Log.d("Firebase Debug", "Event loaded: " + event.getTitle() + ", isMyPost: " + event.isMyPost());
                    myPostsEvents.add(event);
                }

                // Ensure MyPosts only loads if it was selected
                String selectedTab = getIntent().getStringExtra("selectedTab");
                if ("myposts".equals(selectedTab)) {
                    switchTab(myPostsEvents, tabMyPosts);
                }

                adapter.notifyDataSetChanged();
            } else {
                Log.w("Firebase", "Error getting documents.", task.getException());
            }
        });
    }

    /**
     * Loads sample events for testing purposes.
     */
    private void loadSampleEvents() {
        exploreEvents.add(new Event("Event 1", convertToDate("15 Apr 2019"), "#A8E6CF", "", 0, false, false));
        exploreEvents.add(new Event("Event 2", convertToDate("20 Mar 2022"), "#FF8A80",
                "android.resource://" + getPackageName() + "/drawable/sample_image",
                R.drawable.angry_icon, false, false));

        myPostsEvents.add(new Event("My Post", convertToDate("01 Jan 2025"), "#FFD700", "",
                0, false, true));
    }

    /**
     * Switches the current tab and updates the displayed event list.
     */
    private void switchTab(List<Event> targetList, TextView selectedTab) {
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);

        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);


        adapter.setEvents(targetList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFollowToggled(Event event, boolean isFollowed) {
        // Will be helpful for whoever works on logic (e.g., Toasts, DB saving).
    }
}

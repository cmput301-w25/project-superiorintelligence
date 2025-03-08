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
import java.util.List;
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

        // Load sample events into the lists (For now until we can add our own)
        loadSampleEvents();
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

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); // Ensure RecyclerView updates when returning
    }

    private void saveEventToFirebase(Event event) {
        myPostsRef.add(event)
                .addOnSuccessListener(documentReference -> Log.d("Firebase", "Event saved: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firebase", "Error saving event", e));
    }
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
                    switchTab(myPostsEvents, tabMyPosts); // Show MyPosts correctly
                }

                adapter.notifyDataSetChanged();
            } else {
                Log.w("Firebase", "Error getting documents.", task.getException());
            }
        });
    }

    private void loadSampleEvents() {
        // (title, date, overlayColor, imageUrl, emojiResource, isFollowed, isMyPost)

        // Explore events
        exploreEvents.add(new Event("Event 1", "15 Apr 2019", "#A8E6CF", "", 0, false, false));
        exploreEvents.add(new Event("Event 2", "20 Mar 2022", "#FF8A80",
                "android.resource://" + getPackageName() + "/drawable/sample_image",
                R.drawable.angry_icon, false, false));

        // My posts (isMyPost = true)
        myPostsEvents.add(new Event("My Post", "01 Jan 2025", "#FFD700", "",
                0, false, true));
    }

    private void switchTab(List<Event> targetList, TextView selectedTab) {
        // Reset all tabs to normal style
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Bold only the selected tab
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        // Ensure RecyclerView updates correctly
        adapter.setEvents(targetList);
        adapter.notifyDataSetChanged();
    }

    // Called when user toggles follow/unfollow in EventAdapter
    @Override
    public void onFollowToggled(Event event, boolean isFollowed) {
        // Will be helpful for whoever works on logic (e.g., Toasts, DB saving).

    }
}

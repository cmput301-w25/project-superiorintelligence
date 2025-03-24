package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.firebase.Timestamp;

public class MoodFollowingActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> allEvents;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_following);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.mood_events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(this);
        recyclerView.setAdapter(eventAdapter);

        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        // Filter buttons
        Button filterAllButton = findViewById(R.id.filter_all_button);
        Button filterRecentButton = findViewById(R.id.filter_recent_button);
        Button filterWeekButton = findViewById(R.id.filter_week_button);

        // Load all events initially
        loadAllEvents();

        // Filter: All Events (sorted by date, newest first)
        filterAllButton.setOnClickListener(v -> {
            Collections.sort(allEvents, (e1, e2) -> {
                Comparable d1 = e1.getRawDate();
                Comparable d2 = e2.getRawDate();
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1); // Reverse chronological order
            });
            eventAdapter.setEvents(allEvents);
        });

        // Filter: Top 3 Recent Events of Followed Participants
        filterRecentButton.setOnClickListener(v -> {
            List<Event> followedEvents = new ArrayList<>();
            for (Event event : allEvents) {
                if (event.isFollowed()) {
                    followedEvents.add(event);
                }
            }
            Collections.sort(followedEvents, (e1, e2) -> {
                Comparable d1 = e1.getRawDate();
                Comparable d2 = e2.getRawDate();
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1); // Reverse chronological order
            });
            List<Event> top3 = followedEvents.size() > 3 ? followedEvents.subList(0, 3) : followedEvents;
            eventAdapter.setEvents(top3);
        });

        // Filter: Events from the Last Week
        filterWeekButton.setOnClickListener(v -> {
            List<Event> recentWeekEvents = new ArrayList<>();
            long oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
            for (Event event : allEvents) {
                Comparable rawDate = event.getRawDate();
                if (rawDate instanceof Timestamp) {
                    long eventTime = ((Timestamp) rawDate).toDate().getTime();
                    if (eventTime >= oneWeekAgo) {
                        recentWeekEvents.add(event);
                    }
                }
            }
            Collections.sort(recentWeekEvents, (e1, e2) -> {
                Comparable d1 = e1.getRawDate();
                Comparable d2 = e2.getRawDate();
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1); // Reverse chronological order
            });
            eventAdapter.setEvents(recentWeekEvents);
        });
    }

    private void loadAllEvents() {
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING) // Fetch from Firestore in descending order
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    for (var doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setID(doc.getId());
                        allEvents.add(event);
                    }
                    eventAdapter.setEvents(allEvents); // Initial display
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., show a Toast)
                });
    }
}

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodFollowingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodEventAdapter adapter; // You'll need to create this adapter
    private List<Event> moodEvents;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_following);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.mood_events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodEvents = new ArrayList<>();
        adapter = new MoodEventAdapter(moodEvents); // Create this adapter class
        recyclerView.setAdapter(adapter);

        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MoodFollowingActivity.this, HomeActivity.class));
            finish();
        });

        // Filter buttons
        Button filterAllButton = findViewById(R.id.filter_all_button);
        Button filterRecentButton = findViewById(R.id.filter_recent_button);
        Button filterWeekButton = findViewById(R.id.filter_week_button);

        // Load all mood events by default
        loadAllMoodEvents();

        filterAllButton.setOnClickListener(v -> loadAllMoodEvents());
        filterRecentButton.setOnClickListener(v -> loadTop3RecentEvents());
        filterWeekButton.setOnClickListener(v -> loadEventsFromLastWeek());
    }

    private void loadAllMoodEvents() {
        db.collection("moodEvents")
                .orderBy("date", Query.Direction.DESCENDING) // Newest first
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodEvents.clear();
                    for (Event event : querySnapshot.toObjects(Event.class)) {
                        if (event.isFollowed()) { // Assuming only followed events are shown
                            moodEvents.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadTop3RecentEvents() {
        db.collection("moodEvents")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(3) // Limit to 3 most recent
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodEvents.clear();
                    for (Event event : querySnapshot.toObjects(Event.class)) {
                        if (event.isFollowed()) {
                            moodEvents.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadEventsFromLastWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1); // 1 week ago
        Date oneWeekAgo = calendar.getTime();

        db.collection("moodEvents")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodEvents.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    for (Event event : querySnapshot.toObjects(Event.class)) {
                        if (event.isFollowed()) {
                            try {
                                Date eventDate = sdf.parse(event.getDate());
                                if (eventDate != null && eventDate.after(oneWeekAgo)) {
                                    moodEvents.add(event);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}

// Placeholder for the adapter (you'll need to implement this)
class MoodEventAdapter extends RecyclerView.Adapter<MoodEventAdapter.ViewHolder> {
    private List<Event> events;

    public MoodEventAdapter(List<Event> events) {
        this.events = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate your item layout here (e.g., mood_event_item.xml)
        return null; // Replace with actual implementation
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind data to views
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

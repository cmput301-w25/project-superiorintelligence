package com.example.superior_intelligence;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
/**
 * RecentMoodEventFragment
 * Displays recent mood events (sorted by date/time in descending order)
 */
public class RecentMoodEventFragment extends Fragment {
    private RecyclerView recyclerView ;
    private EventAdapter adapter;
    private final ArrayList<Event> eventList = new ArrayList<>();
    private RecentMoodEventRepository repository;

    /**
     * Creates and returns the fragment's view hierarchy.
     *
     * @param inflater The LayoutInflater to inflate views
     * @param container The parent view group to attach to
     * @param savedInstanceState Saved state from previous instance
     * @return The inflated fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_mood_event, container, false);
        repository = new RecentMoodEventRepository();
        recyclerView = view.findViewById(R.id.recentMoodRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter ( eventList);
        recyclerView.setAdapter(adapter );
        loadRecentEvents();
        return view;
    }

    /**
     * Loads recent mood events from repository and updates the RecyclerView.
     * Clears existing events and populates with new data on success.
     * Logs errors if event loading fails.
     */
    private void loadRecentEvents(){
        repository.getRecentMoodEvents(events ->{
            eventList.clear();
            eventList.addAll(events);
            adapter.notifyDataSetChanged();
        } , exception ->{
            Log.e("RecentEventFragment", "Error loading events", exception); }) ;

    }}
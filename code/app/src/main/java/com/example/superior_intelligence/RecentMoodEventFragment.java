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
import com.example.superior_intelligence.Event;
import com.example.superior_intelligence.RecentMoodEventRepository;
import com.example.superior_intelligence.R;
import com.example.superior_intelligence.EventAdapter;
import java.util.ArrayList;
/**
 * RecentMoodEventFragment
 *
 * Displays recent mood events (sorted by date/time in descending order)
 */
public class RecentMoodEventFragment extends Fragment {
    private RecyclerView recyclerView ;
    private EventAdapter adapter;
    private final ArrayList<Event> eventList = new ArrayList<>();
    private RecentMoodEventRepository repository;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {View view = inflater.inflate(R.layout.fragment_recent_mood_event, container, false);
        repository = new RecentMoodEventRepository();
        recyclerView = view.findViewById(R.id.recentMoodRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter ( eventList);
        recyclerView.setAdapter(adapter );
        loadRecentEvents();
        return view;}
    private void loadRecentEvents(){
        repository.getRecentMoodEvents(events ->{
            eventList.clear();
            eventList.addAll(events);
            adapter.notifyDataSetChanged();
        } , exception ->{
            Log.e("RecentEventFragment", "Error loading events", exception); }) ;

    }}
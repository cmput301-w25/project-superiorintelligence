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
import com.example.superior_intelligence.R;

import com.example.superior_intelligence.Mood;
import com.example.superior_intelligence.MoodAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;


import java.util.List;

/**
 * RecentMoodEventFragment
 *
 * <p>
 *     Displays the most recent Mood Events from Firestore ('MyPosts' collection),
 *     sorted by date/time descending. The list is shown in a RecyclerView.
 * </p>
 *
 * <p>References:
 * <a href="https://stackoverflow.com/questions/5608720/">Sample Link</a>
 * </p>
 */
public class RecentMoodEventFragment extends Fragment {
    private RecyclerView recyclerView;
    private MoodAdapter adapter;
    private final List<Mood> moodEventsList = new ArrayList<>();
    private FirebaseFirestore db;
    private com.example.superior_intelligence.Mood Mood;

    public RecentMoodEventFragment() {}
    @Override
    public View onCreateView(
            @NonNull LayoutInflater  inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return  inflater.inflate(R.layout.fragment_recent_mood_event,  container,  false);}
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        recyclerView =  view.findViewById(R.id.recent_mood_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MoodAdapter(moodEventsList);
        recyclerView.setAdapter(adapter);
        loadRecentMoodEvents();}
    /**
     * loadRecentMoodEvents
     *
     * <p>
     *     Queries the 'MyPosts' collection in Firestore, ordering by 'date' in descending order.
     *     This method updates the adapter once the data is retrieved.
     * </p>
     */


    private void loadRecentMoodEvents()
    {
        db.collection("MyPosts")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(querySnapshot ->{
                    List<Mood> tempList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {
                        Mood event = doc.toObject(Mood.class);
                        tempList.add(Mood);}
                    adapter.updateData(tempList);
                })
                .addOnFailureListener(e -> {
                    Log.e("RecentMoodEventFragment", "Error fetching recent moods", e);
                });
    }


}
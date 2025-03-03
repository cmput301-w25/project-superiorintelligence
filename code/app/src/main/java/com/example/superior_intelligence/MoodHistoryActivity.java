package com.example.superior_intelligence;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);
        db = FirebaseFirestore.getInstance();
        loadMoodEvents();}




    private void loadMoodEvents() {
        progressBar.setVisibility(View.VISIBLE);
        CollectionReference moodsRef = db.collection("MoodEvents");
        moodsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    moodList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Mood mood = doc.toObject(Mood.class);
                        moodList.add(mood);}
                    if (moodList.isEmpty())
                    {
                        emptyTextView.setVisibility(View.VISIBLE);}
                    else {emptyTextView.setVisibility(View.GONE);}
                    moodAdapter.notifyDataSetChanged();})
                .addOnFailureListener(e -> {


                    progressBar.setVisibility(View.GONE);
                    Log.e("MoodHistory", "Error loading moods", e);});
    }
}

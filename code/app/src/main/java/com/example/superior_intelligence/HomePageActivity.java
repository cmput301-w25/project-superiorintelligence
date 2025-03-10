package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Set;

/**
 *  This class is for the add button activity of homepage that redirects user to create mood event page
 *
 */
public class HomePageActivity extends AppCompatActivity /*implements MoodEventTitleFragment.MoodEventDialogListener*/ {

    private ImageButton addButton; // Declare add button
    private ListView moodListView;

    /*
    //==========DATABASE MOOD EVENT==========
    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    private DocumentReference docRef;
    //add firebase to interact with database
    private FirebaseFirestore db;
    //change name from demo (moviesRef -> movieRef)
    private CollectionReference moodsRef;
    //=======================================
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page); // Ensure correct layout file

        //========SET VIEW=================
        // Initialize the add button
        addButton = findViewById(R.id.addButton);
        //moodListView = findViewById(R.id.recycler_view);

        /*
        //=============SET DATABASE==============
        // obtain instance of database
        db = FirebaseFirestore.getInstance();
        moodsRef = db.collection("Mood events");

        // create mood array
        moodArrayList = new ArrayList<>();
        moodArrayAdapter = new MoodArrayAdapter(this, moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
        //=======================================
        */

        //Set click listener to navigate to Create Mood Event Page
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, MoodCreateAndEditActivity.class);
                startActivity(intent);
            }
        });


       /*addButton.setOnClickListener(view -> {
           MoodEventTitleFragment moodEventTitleFragment = new MoodEventTitleFragment();
           moodEventTitleFragment.show(getSupportFragmentManager(), "Give your mood event a name!");
       });*/

        /*
        //looking for changes in database
        moodsRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                moodArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    //TO BE ADDED FOR MORE MOOD EVENT INFO
                    String moodTitle = snapshot.getString("mood title");
                    String moodExplanation = snapshot.getString("mood explanation");
                    String emotionalState = snapshot.getString("emotional state");

                    moodArrayList.add(new Mood(moodTitle, moodExplanation, emotionalState));
                }
                moodArrayAdapter.notifyDataSetChanged();
            }


        }); */
    }

    /*
    @Override
    public void addMoodTitle(String title, Boolean addStatus) {
        Mood mood = new Mood(title);
        docRef.set(mood);

        moodArrayList.add(mood);
        moodArrayAdapter.notifyDataSetChanged();

        // Go to mood create page when user enter title
        new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this, MoodCreateAndEditActivity.class);
                startActivity(intent);
            }
        };
    }
    */

}

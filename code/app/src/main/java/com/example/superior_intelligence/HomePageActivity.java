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
 *  This class is the Homepage for the app with button to add mood event and ...
 *
 */
public class HomePageActivity extends AppCompatActivity {

    private ImageButton addButton; // Declare add button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page); // Ensure correct layout file

        //========SET VIEW=================
        // Initialize the add button
        addButton = findViewById(R.id.addButton);

        //Set click listener to navigate to Create Mood Event Page
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, MoodCreateAndEditActivity.class);
                startActivity(intent);
            }
        });

    }
}
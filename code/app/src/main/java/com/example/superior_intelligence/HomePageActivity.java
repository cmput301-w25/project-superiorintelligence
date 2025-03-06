package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private ImageButton addButton; // Declare add button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page); // Ensure correct layout file

        // Initialize the add button
        addButton = findViewById(R.id.addButton);

        // Set click listener to navigate to Create Mood Event Page
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, MoodCreateAndEditActivity.class);
                startActivity(intent);
            }
        });
    }
}

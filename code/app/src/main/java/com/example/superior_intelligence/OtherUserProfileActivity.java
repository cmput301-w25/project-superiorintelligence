package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OtherUserProfileActivity extends AppCompatActivity {
    private TextView profileName, profileUsername;
    private String username;
    private Userbase userbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otheruser_profile);

        // Retrieve username from intent
        username = getIntent().getStringExtra("username");

        // Initialize Userbase
        userbase = Userbase.getInstance();

        // Find views
        profileName = findViewById(R.id.profile_name);
        profileUsername = findViewById(R.id.profile_username);
        ImageButton backButton = findViewById(R.id.profile_back_button);

        // Load user profile details
        loadUserProfile();

        // Back button returns to previous page
        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Loads the profile details of the selected user using Userbase.
     */
    private void loadUserProfile() {
        if (username == null) {
            Log.e("OtherUserProfileActivity", "Username is null, cannot load profile.");
            return;
        }

        userbase.getUserDetails(username, (exists, fetchedUsername, name) -> {
            if (exists) {
                profileUsername.setText("@" + fetchedUsername);
                profileName.setText(name);
            } else {
                Log.e("OtherUserProfileActivity", "User not found.");
            }
        });
    }
}

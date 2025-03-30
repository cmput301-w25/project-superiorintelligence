package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OtherUserProfileActivity extends AppCompatActivity {
    TextView profileName;
    TextView profileUsername;
    String username;
    String currentUser;
    Userbase userbase;
    Button followRequestButton;
    FollowManager.FollowStatus followStatus = FollowManager.FollowStatus.NOT_FOLLOWING;

    enum FollowStatus {
        NOT_FOLLOWING, REQUEST_SENT, FOLLOWING
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFollowStatus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otheruser_profile);

        // Retrieve username from intent
        username = getIntent().getStringExtra("username");
        currentUser = User.getInstance().getUsername();

        // Initialize Userbase
        userbase = Userbase.getInstance();

        // Find views
        profileName = findViewById(R.id.profile_name);
        profileUsername = findViewById(R.id.profile_username);
        followRequestButton = findViewById(R.id.follow_request_button);
        ImageButton backButton = findViewById(R.id.profile_back_button);

        // Load user profile details
        loadUserProfile();

        // Check follow status
        checkFollowStatus();

        // Back button returns to previous page
        backButton.setOnClickListener(view -> finish());

        // Handle Follow Button Click
        followRequestButton.setOnClickListener(view -> handleFollowButtonClick());
    }

    /**
     * Loads the profile details of the selected user using Userbase.
     */
    void loadUserProfile() {
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
                Log.e("OtherUserProfileActivity", "User not found: " + username);
            }
        });
    }

    private void checkFollowStatus() {
        FollowManager.checkFollowStatus(currentUser, username, userbase, status -> {
            followStatus = status;
            FollowManager.updateFollowButton(followRequestButton, status);
        });
    }


    private void handleFollowButtonClick() {
        if (followStatus == null) return;

        if (followStatus == FollowManager.FollowStatus.FOLLOWING) {
            userbase.unfollowUser(currentUser, username, success -> {
                followStatus = FollowManager.FollowStatus.NOT_FOLLOWING;
                FollowManager.updateFollowButton(followRequestButton, followStatus);
            });
        } else if (followStatus == FollowManager.FollowStatus.NOT_FOLLOWING) {
            userbase.sendFollowRequest(currentUser, username, success -> {
                followStatus = FollowManager.FollowStatus.REQUEST_SENT;
                FollowManager.updateFollowButton(followRequestButton, followStatus);
            });
        }
    }
}

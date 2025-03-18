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
    private TextView profileName, profileUsername;
    private String username;
    private String currentUser;
    private Userbase userbase;
    private Button followRequestButton;

    private enum FollowStatus {
        NOT_FOLLOWING, REQUEST_SENT, FOLLOWING
    }
    private FollowStatus followStatus = FollowStatus.NOT_FOLLOWING; // Default

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

    private void checkFollowStatus() {
        userbase.getUserFollowing(currentUser, followingList -> {
            if (followingList.contains(username)) {
                followStatus = FollowStatus.FOLLOWING;
            } else {
                userbase.checkFollowRequest(currentUser, username, isRequestSent -> {
                    followStatus = isRequestSent ? FollowStatus.REQUEST_SENT : FollowStatus.NOT_FOLLOWING;
                    updateFollowButton();
                });
            }
            updateFollowButton();
        });
    }

    private void updateFollowButton() {
        switch (followStatus) {
            case FOLLOWING:
                followRequestButton.setText("Unfollow");
                followRequestButton.setEnabled(false);
                break;
            case REQUEST_SENT:
                followRequestButton.setText("Pending Request");
                followRequestButton.setEnabled(false);
                break;
            case NOT_FOLLOWING:
                followRequestButton.setText("Follow");
                followRequestButton.setEnabled(true);
                break;
        }
    }

    private void handleFollowButtonClick() {
        if (followStatus == FollowStatus.NOT_FOLLOWING) {
            userbase.sendFollowRequest(currentUser, username, success -> {
                if (success) {
                    followStatus = FollowStatus.REQUEST_SENT;
                    updateFollowButton();
                    userbase.addNotification(username,currentUser + " wants to follow your mood events.");
                } else {
                    Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (followStatus == FollowStatus.FOLLOWING) {
            userbase.unfollowUser(currentUser, username, success -> {
                if (success) {
                    followStatus = FollowStatus.NOT_FOLLOWING;
                    updateFollowButton();
                    Toast.makeText(this, "Unfollowed " + username, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to unfollow", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

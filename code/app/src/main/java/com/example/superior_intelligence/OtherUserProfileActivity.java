package com.example.superior_intelligence;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for viewing and interacting with another user's profile.
 * Allows the current user to view profile details and manage follow status
 * (follow/unfollow/send request) for the viewed user.
 */
public class OtherUserProfileActivity extends AppCompatActivity {
    TextView profileName;
    TextView profileUsername;
    String username;
    String currentUser;
    Userbase userbase;
    Button followRequestButton;
    FollowManager.FollowStatus followStatus = FollowManager.FollowStatus.NOT_FOLLOWING;

    /**
     * Checks and updates follow status when activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkFollowStatus();
    }

    /**
     * Initializes the activity and sets up profile view components.
     * @param savedInstanceState Saved instance state or null
     */
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

    /**
     * Checks the current follow relationship status between users.
     * Updates UI button state based on follow status.
     */
    private void checkFollowStatus() {
        FollowManager.checkFollowStatus(currentUser, username, userbase, status -> {
            followStatus = status;
            FollowManager.updateFollowButton(followRequestButton, status);
        });
    }


    /**
     * Handles follow button click actions based on current follow status.
     * update UI status
     */
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

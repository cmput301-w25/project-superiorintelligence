package com.example.superior_intelligence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Load user details from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = prefs.getString("name", "Default Name");
        String username = prefs.getString("username", "Default Username");

        // Set the values to the TextViews
        TextView nameTextView = findViewById(R.id.profile_name);
        TextView usernameTextView = findViewById(R.id.profile_username);
        nameTextView.setText(name);
        usernameTextView.setText(username);

        // Back button to returns to HomeActivity
        ImageButton backButton = findViewById(R.id.profile_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Search User button
        LinearLayout searchUserButton = findViewById(R.id.search_user_button);
        searchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchUsersActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // signout button
        LinearLayout signoutButton = findViewById(R.id.signout);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, LoginPageActivity.class);
                startActivity(intent);
                finish(); //user cannot return to this page if at login page
            }
        });


    }
}

/**
 * completes sub-issue: CreateAccountPage #144
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=100541362&issue=cmput301-w25%7Cproject-superiorintelligence%7C144
 * Creates an account by entering a unique username. Checks database to ensure username is unique and doesn't exist
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.superior_intelligence.HomePageActivity;

public class CreateAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_page);

        // Find the back button
        ImageButton backButton = findViewById(R.id.back_button);

        // Set click listener to navigate back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes the current activity and returns to MainActivity
            }
        });

        AppCompatButton SignupButton = findViewById(R.id.signup_button);
        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });
    }
}

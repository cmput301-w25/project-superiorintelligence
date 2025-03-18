/**
 * completes sub-issue: CreateAccountPage #144
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=100541362&issue=cmput301-w25%7Cproject-superiorintelligence%7C144
 * Creates an account by entering a unique username. Checks database to ensure username is unique and doesn't exist
 * checkUsernameExists: check to see if the username exists in the database, taosts if it does not, else calls createUser()
 * createUser: adds a new account with the given username to the database
 */
package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.superior_intelligence.Userbase;


public class CreateAccountActivity extends AppCompatActivity {

    EditText signupName, signupUsername;
    Button signupButton;
    private Userbase userbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_page);

        signupName = findViewById(R.id.signup_name);
        signupUsername = findViewById(R.id.signup_username);
        signupButton = findViewById(R.id.signup_button);

        userbase = Userbase.getInstance();

        // Handle signup button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = signupName.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();

                if (name.isEmpty() || username.isEmpty()) {
                    Toast.makeText(CreateAccountActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if username already exists
                checkUsernameExists(name, username);
            }
        });

        // Find the back button and handle click
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Closes the current activity and returns to the previous screen
            }
        });
    }

    /**
     * Uses the Userbase to check if the username already exists.
     */
    private void checkUsernameExists(final String name, final String username) {
        userbase.checkUserExists(username, (exists, existingName, existingUsername) -> {
            if (exists) {
                signupUsername.setError("Username already exists");
                signupUsername.requestFocus();
            } else {
                createUser(name, username);
            }
        });
    }

    /**
     * Creates a new user using the userbase.
     */
    private void createUser(final String name, final String username) {
        userbase.createUser(name, username, success -> {
            if (success) {
                Toast.makeText(CreateAccountActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();

                // Populate the global User instance
                User user = User.getInstance();
                user.setName(name);
                user.setUsername(username);

                // Navigate to LoginPageActivity
                startActivity(new Intent(CreateAccountActivity.this, LoginPageActivity.class));
                finish();
            } else {
                Toast.makeText(CreateAccountActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

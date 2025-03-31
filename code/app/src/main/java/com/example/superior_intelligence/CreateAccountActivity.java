package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for creating a new user account.
 * This activity provides the UI for the user to input their name, username, and password
 * to create an account. It performs validation on the input fields, checks if the username
 * already exists, and then creates the new user by hashing the password and storing the user data.
 */
public class CreateAccountActivity extends AppCompatActivity {

    EditText signupName, signupUsername, signupPassword;
    Button signupButton;
    private Userbase userbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_page);

        signupName = findViewById(R.id.signup_name);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);

        userbase = Userbase.getInstance();

        // Handle signup button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = signupName.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
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
     * Checks if the given username already exists in the database.
     *
     * @param name The name of the user.
     * @param username The username chosen by the user.
     */
    private void checkUsernameExists(final String name, final String username) {
        userbase.checkUserExists(username, (exists, existingName, existingUsername, ignoredPassword) -> {
            if (exists) {
                signupUsername.setError("Username already exists");
                signupUsername.requestFocus();
            } else {
                createUser(name, username);
            }
        });
    }

    /**
     * Creates a new user account.
     *
     * @param name The name of the user.
     * @param username The username chosen by the user.
     */
    private void createUser(final String name, final String username) {
        // Retrieve the password from the EditText and hash it using SHA-256.
        final String plainPassword = signupPassword.getText().toString().trim();
        final String hashedPassword = PasswordHasher.hashPassword(plainPassword);

        userbase.createUser(name, username, hashedPassword, success -> {
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

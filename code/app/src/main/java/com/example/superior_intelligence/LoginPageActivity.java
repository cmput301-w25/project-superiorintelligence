/**
 * completes sub issue: Create log-in screen #123
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=99937029&issue=cmput301-w25%7Cproject-superiorintelligence%7C123
 * Connects to login_page.xml, provide button to enter CreateAccount.java
 * if user enters login and enters username, will check database to see if existing
 * validateUsername returns true if a username was given, false if empty (bool)
 * checkUser: checks if username exist in db, error else update db and go to login page
 */

package com.example.superior_intelligence;
import com.example.superior_intelligence.Userbase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginPageActivity extends AppCompatActivity {

    EditText loginUsername;
    Button loginButton;
    Button signUpButton;
    //TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        loginUsername = findViewById(R.id.login_username);
        //signupRedirectText = findViewById(R.id.signupRedirectText);
        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.signup_page_button);

        // Find the back button
        ImageButton backButton = findViewById(R.id.back_button);
        // Set click listener to navigate back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPageActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // Close current activity to prevent returning to it
            }
        });

        // login page to sign up page
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPageActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        // login button pressed, go to home page
        AppCompatButton loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() | !validateUsername()) {
                } else {
                    checkUser();
                }
            }
        });
    }

    /**
     * Validates that the username field is not empty.
     * @return true if valid, false otherwise.
     */
    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    /**
     * Checks if the user exists in the database.
     * If the user exists, the global User instance is updated and HomeActivity is launched.
     * Otherwise, an error is displayed.
     */
    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        Userbase userbase = new Userbase();
        userbase.checkUserExists(userUsername, (exists, name, username) -> {
            if (exists) {
                // Save user details in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("name", name);
                editor.putString("username", username);
                editor.apply(); // Save changes
                // Populate the global User instance
                User user = User.getInstance();
                user.setName(name);
                user.setUsername(username);
                // Navigate to HomeActivity
                Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                loginUsername.setError("User does not exist");
                loginUsername.requestFocus();
            }
        });
    }
}
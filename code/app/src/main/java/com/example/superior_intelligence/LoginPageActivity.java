/**
 * completes sub issue: Create log-in screen #123
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=99937029&issue=cmput301-w25%7Cproject-superiorintelligence%7C123
 * Connects to login_page.xml, provide button to enter CreateAccount.java
 * if user enters login and enters username, will check database to see if existing
 * validateUsername returns true if a username was given, false if empty (bool)
 * checkUser: checks if username exist in db, error else update db and go to login page
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.superior_intelligence.CreateAccountActivity;
import com.example.superior_intelligence.HomePageActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

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
                finish(); // Closes the current activity and returns to MainActivity
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

    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("username", userUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        loginUsername.setError(null);

                        // User exists → Go to HomePageActivity
                        Intent intent = new Intent(LoginPageActivity.this, HomePageActivity.class);
                        startActivity(intent);
                    } else {
                        // User doesn't exist
                        loginUsername.setError("User does not exist");
                        loginUsername.requestFocus();
                    }
                })
                .addOnFailureListener(e -> { // error handling
                    loginUsername.setError("Failed to connect to Firestore");
                    loginUsername.requestFocus();
                });
    }

}
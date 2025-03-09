/**
 * completes sub-issue: CreateAccountPage #144
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=100541362&issue=cmput301-w25%7Cproject-superiorintelligence%7C144
 * Creates an account by entering a unique username. Checks database to ensure username is unique and doesn't exist
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccountActivity extends AppCompatActivity {

    EditText signupName, signupUsername;
    Button signupButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_page);

        signupName = findViewById(R.id.signup_name);
        signupUsername = findViewById(R.id.signup_username);
        signupButton = findViewById(R.id.signup_button);

        db = FirebaseFirestore.getInstance();

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

    private void checkUsernameExists(String name, String username) {
        CollectionReference userRef = db.collection("users");

        userRef.whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Username already exists
                        signupUsername.setError("Username already exists");
                        signupUsername.requestFocus();
                    } else {
                        // Username is unique → Create account
                        createUser(name, username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateAccountActivity.this, "Failed to check username", Toast.LENGTH_SHORT).show();
                });
    }

    private void createUser(String name, String username) {
        HelperClass helperClass = new HelperClass(name, username);

        db.collection("users")
                .add(helperClass)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateAccountActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAccountActivity.this, LoginPageActivity.class);
                    startActivity(intent);
                    finish(); // Close current activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateAccountActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                });
    }
}

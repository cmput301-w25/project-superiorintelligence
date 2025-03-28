package com.example.superior_intelligence;

import com.example.superior_intelligence.Userbase;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginPageActivity extends AppCompatActivity {

    EditText loginUsername;
    EditText loginPassword;
    Button loginButton;
    Button signUpButton;
    private Userbase userbase = Userbase.getInstance();
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.signup_page_button);
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Find the back button and set click listener to navigate back
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPageActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Navigate to CreateAccountActivity when sign-up button is pressed
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPageActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        // Login button pressed, check validation then attempt login
        AppCompatButton loginBtn = findViewById(R.id.login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() || !validatePassword()) {
                    return;
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
     * Validates that the password field is not empty.
     * @return true if valid, false otherwise.
     */
    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    /**
     * Checks if the user exists in the database.
     * If the user exists, compares the hashed entered password with the stored hashed password.
     * If they match, updates SharedPreferences, the global User instance, and navigates to HomeActivity.
     * Otherwise, shows an error.
     */
    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();
        loadingIndicator.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Retrieve the stored hash via the callback.
        userbase.checkUserExists(userUsername, (exists, name, username, passwordFromDB) -> {
            if (exists) {
                // Hash the entered password using SHA-256 before comparison.
                String hashedEnteredPassword = PasswordHasher.hashPassword(userPassword);
                if (hashedEnteredPassword.equals(passwordFromDB)) {
                    // Save user details in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name", name);
                    editor.putString("username", username);
                    editor.apply();

                    // Populate the global User instance
                    User user = User.getInstance();
                    user.setName(name);
                    user.setUsername(username);

                    // Proceed to HomeActivity after ensuring FirebaseAuth and Database readiness.
                    ensureFirebaseAuth(() -> ensureDatabaseIsReady(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        startActivity(new Intent(LoginPageActivity.this, HomeActivity.class));
                        finish();
                    }));
                } else {
                    // Password does not match, show error.
                    loginPassword.setError("Incorrect password");
                    loginPassword.requestFocus();
                    loadingIndicator.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                }
            } else {
                loginUsername.setError("User does not exist");
                loginUsername.requestFocus();
                loadingIndicator.setVisibility(View.GONE);
                loginButton.setEnabled(true);
            }
        });
    }

    /**
     * Ensures Firestore has finished loading before switching activities.
     */
    private void ensureDatabaseIsReady(Runnable onReady) {
        Database database = Database.getInstance();
        database.loadEventsFromFirebase(User.getInstance(), (myPosts, explore, followed) -> {
            if (myPosts != null && explore != null && followed != null) {
                onReady.run();
            } else {
                Toast.makeText(this, "Failed to fetch data. Try again.", Toast.LENGTH_SHORT).show();
                loadingIndicator.setVisibility(View.GONE);
                loginButton.setEnabled(true);
            }
        });
    }

    /**
     * Ensures FirebaseAuth has an active session using anonymous login.
     */
    private void ensureFirebaseAuth(Runnable onAuthenticated) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            onAuthenticated.run();
        } else {
            FirebaseAuth.getInstance().signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            onAuthenticated.run();
                        } else {
                            Toast.makeText(this, "Firebase authentication failed.", Toast.LENGTH_SHORT).show();
                            loadingIndicator.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                        }
                    });
        }
    }
}

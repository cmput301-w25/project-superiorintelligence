/**
 * completes sub issue: Create log-in screen #123
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=99937029&issue=cmput301-w25%7Cproject-superiorintelligence%7C123
 * Connects to login_page.xml, provide button to enter CreateAccount.java
 * if user enters login and enters username, will check database to see if existing
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.superior_intelligence.CreateAccountActivity;
import com.example.superior_intelligence.HomePageActivity;

public class LoginPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

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
        AppCompatButton SignupButton = findViewById(R.id.signup_button);
        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPageActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        // login button pressed, go to home page
        AppCompatButton loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
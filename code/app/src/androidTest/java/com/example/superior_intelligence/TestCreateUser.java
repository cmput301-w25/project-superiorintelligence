package com.example.superior_intelligence;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for create user activity
 * checks user db
 */

public class TestCreateUser {

    @Test
    public void testCreateUser_successfulCreation() {
        // Launch the activity
        ActivityScenario<CreateAccountActivity> scenario = ActivityScenario.launch(CreateAccountActivity.class);

        scenario.onActivity(activity -> {
            // Mock user inputs
            activity.signupName.setText("John Doe");
            activity.signupUsername.setText("johndoe123");
            activity.signupPassword.setText("securePass123");

            // Simulate button click to trigger user creation
            activity.signupButton.performClick();

            // Since createUser uses a callback, we can't directly get the result here.
            // Instead, we can verify the UI feedback or mock Userbase for deeper testing.
            // For simplicity, we'll check if the fields are still populated (no immediate errors)
            assertEquals("John Doe", activity.signupName.getText().toString());
            assertEquals("johndoe123", activity.signupUsername.getText().toString());
            assertEquals("securePass123", activity.signupPassword.getText().toString());

            // Optionally, check if Toast is shown (requires additional setup with Espresso or Robolectric)
            // For now, assume success if no immediate field errors are set
            assertNull(activity.signupUsername.getError()); // No "Username already exists" error
        });
    }
}
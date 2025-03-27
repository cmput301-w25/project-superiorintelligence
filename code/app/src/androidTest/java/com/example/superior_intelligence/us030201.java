/**
 * testing SearchUsersActivity
 * see comments below for method/test cases
 */

package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class us030201 {

    @Before
    public void setUp() {
        // Connect to Firestore emulator
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080);

        // Preload test data
        db.collection("users").document("user1").set(new HelperClass("MrTest", "testing"));

        // Set current user (to avoid filtering issues in this simple test)
        User.getInstance().setUsername("differentuser");

        // Launch activity
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), SearchUsersActivity.class);
        ActivityScenario.launch(intent);
    }

    @Test
    public void testPartialUsernameSearch_displaysMatchingUser() {
        // Type "tes" into the EditText
        onView(withId(R.id.editText)).perform(typeText("tes"));

        // Click the search button
        onView(withId(R.id.user_search_button)).perform(click());

        // Wait briefly for Firestore (simple delay, not ideal but keeps it minimal)
        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if "testing" is displayed
        onView(withText("testing")).check(matches(isDisplayed()));
    }
}
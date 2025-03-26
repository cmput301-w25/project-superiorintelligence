/**
 * The purpose of this code is to test and ensure the correct impplementation of CreateAccountActivity and LoginPageActivity
 * test CreateUniqueUserTest ensures newly created usernames are unique and do not already exist in the database
 * test LoginTest ensures users may only log in if they have a username found in the database
 * tests signout as well
 * this file uses espresso for the test implementation
 */
package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class us030101 {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        // Use emulator for Firestore connection
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    // Test signup, login and signout functionality
    // ensure emulator is cleared before each run
    @Test
    public void testCreateAndLoginFlow() throws InterruptedException {
        String testName = "Test User";
        String testUsername = "testUser123";
        String nonExistentUsername = "doesNotExist";

        // go to main page
        onView(withId(R.id.login_button_login_page)).perform(click());

        // Create a new account
        onView(withId(R.id.signup_page_button)).perform(click());
        onView(withId(R.id.signup_name)).perform(replaceText(testName));
        onView(withId(R.id.signup_username)).perform(replaceText(testUsername));
        closeSoftKeyboard();
        onView(withId(R.id.signup_button)).perform(click());

        // Allow time for Firestore to update
        Thread.sleep(2000);
        onView(withId(R.id.signup_page_button)).perform(click()); // go back to login

        // Try creating another account with the same username (should fail)
        onView(withId(R.id.signup_name)).perform(replaceText("Another User"));
        onView(withId(R.id.signup_username)).perform(replaceText(testUsername));
        closeSoftKeyboard();
        onView(withId(R.id.signup_button)).perform(click());

        // Check for "Username already exists" error
        onView(withId(R.id.signup_username)).check(matches(hasErrorText("Username already exists")));

        // Try logging in with a non-existent username (should fail)
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.login_username)).perform(replaceText(nonExistentUsername));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());

        // Check for "User does not exist" error
        onView(withId(R.id.login_username)).check(matches(hasErrorText("User does not exist")));

        // Successfully log in with the created username
        onView(withId(R.id.login_username)).perform(replaceText(testUsername));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());

        //Currently on app home page, logged in under testName and testUsername
        Thread.sleep(2000);
        onView(withId(R.id.profile_image)).perform(click()); // Navigate to profile page
        onView(withId(R.id.signout)).perform(click()); // press signout button

    }

}


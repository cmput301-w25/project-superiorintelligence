package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.content.Intent;
import android.os.SystemClock;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class us030201_us030301 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Sets up Firebase emulator and ensures test user exists before each test.
     */
    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);
        ensureUserExists("testUser", "Test User", "TestPass");
        Intents.init();
    }


    @Test
    public void testSearchForAndViewOtherUsersProfile() throws InterruptedException {
        ensureUserExists("paul", "Paul B", "SomePass");
        ensureUserExists("carsy", "Carson", "SomePass");
        ensureUserExists("seth", "pls", "pls give good grade");

        loginAs("testUser","TestPass");

        onView(withId(R.id.profile_image)).perform(click());
        SystemClock.sleep(2000);

        // Go to "Search Followers"
        onView(withId(R.id.search_user_button)).perform(click());
        SystemClock.sleep(1000);

        // Type part of the user's username (e.g., "pa" for "paul")
        onView(withId(R.id.search_bar)).perform(typeText("pa"));
        ViewActions.closeSoftKeyboard();
        SystemClock.sleep(2000); // Give Firestore a moment to respond

        onView(withId(R.id.user_search_button)).perform(click());

        // Click on the matching username in the search results
        onView(withText("paul")).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.profile_username)).check(matches(withText("@paul")));
    }


    /**
     * Logs in using the given username.
     */
    private void loginAs(String username, String password) throws InterruptedException {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        ViewActions.closeSoftKeyboard();

        onView(withId(R.id.login_password)).perform(typeText(password));
        ViewActions.closeSoftKeyboard();

        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000);
    }

    /**
     * Creates a test user in the Firestore emulator if not already present.
     */
    private void ensureUserExists(String username, String name, String rawPassword) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new java.util.ArrayList<>());
        user.put("following", new java.util.ArrayList<>());

        String hashedPassword = PasswordHasher.hashPassword(rawPassword);
        user.put("password", hashedPassword);

        db.collection("users").document(username).set(user)
                .addOnCompleteListener(task -> latch.countDown());

        latch.await();
    }




}

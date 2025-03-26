package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.util.Log;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class us010401_usus010501_us010601 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirestoreIdlingResource userIdlingResource = new FirestoreIdlingResource("users");
    private final FirestoreIdlingResource postsIdlingResource = new FirestoreIdlingResource("MyPosts");

    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);
        ensureUserExists("testUser", "Test User");
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(userIdlingResource, postsIdlingResource);
        clearFirestore();
    }

    @Test
    public void testViewEditDeleteMoodEvent() throws InterruptedException {
        loginAs("testUser");

        // 🧠 Register idling resources only AFTER login
        IdlingRegistry.getInstance().register(userIdlingResource, postsIdlingResource);

        // Create mood event
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.mood_event_title)).perform(typeText("Original Title"));
        closeSoftKeyboard();

        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(Matchers.allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());

        onView(withId(R.id.trigger_response)).perform(typeText("Automated Reason"));
        closeSoftKeyboard();

        onView(withId(R.id.situation_arrow)).perform(click());
        onData(Matchers.allOf(is(instanceOf(String.class)), is("Alone"))).perform(click());
        onView(withId(R.id.selected_situation)).check(matches(withText("Alone")));

        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        onView(withId(R.id.public_checkbox)).perform(click());
        onView(anyOf(withText("CONFIRM"), withText("POST"))).perform(click());

        postsIdlingResource.waitForFirestoreUpdate();

        onView(withId(R.id.tab_myposts)).perform(click());
        onView(withText("Original Title")).perform(click());
        onView(withId(R.id.event_detail_title)).check(matches(withText("Original Title")));

        // Edit mood event
        onView(withId(R.id.edit_button)).perform(click());
        onView(withId(R.id.mood_event_title)).perform(replaceText("Edited Title"));
        closeSoftKeyboard();
        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        onView(anyOf(withText("CONFIRM"), withText("POST"))).perform(click());

        postsIdlingResource.waitForFirestoreUpdate();

        onView(withId(R.id.tab_myposts)).perform(click());
        onView(withText("Edited Title")).check(matches(isDisplayed()));

        // Delete mood event
        onView(withText("Edited Title")).perform(click());
        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("DELETE")).perform(click());

        postsIdlingResource.waitForFirestoreUpdate();

        onView(withId(R.id.tab_myposts)).perform(click());
        onView(withText("Edited Title")).check(doesNotExist());
    }

    private void loginAs(String username) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
    }

    private void ensureUserExists(String username, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new ArrayList<>());
        user.put("following", new ArrayList<>());

        db.collection("users").document(username).set(user)
                .addOnCompleteListener(task -> latch.countDown());

        latch.await();
    }

    private void clearFirestore() {
        String projectId = "moodgram";
        try {
            URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.getResponseCode();
            connection.disconnect();
        } catch (IOException e) {
            Log.e("FirestoreClearError", "Failed to clear emulator DB", e);
        }
    }
}

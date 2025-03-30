package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static org.hamcrest.Matchers.allOf;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;


@RunWith(AndroidJUnit4.class)
public class TestNotification {

    static {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);
    }

    @Rule
    public ActivityTestRule<NotificationActivity> activityRule =
            new ActivityTestRule<>(NotificationActivity.class, true, false);

    @Before
    public void setUp() throws InterruptedException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        auth.signInAnonymously().addOnCompleteListener(task -> latch.countDown());
        latch.await();

        User.getInstance().setUsername("testUser");

        // Ensure users exist before using follow_requests logic
        ensureUserExists("alice");
        ensureUserExists("bob");
        ensureUserExists("testUser");

        ensureFollowRequestExists("alice", "testUser");
        ensureFollowRequestExists("testUser", "bob");

        activityRule.launchActivity(new Intent());
        SystemClock.sleep(2000);
    }

    private void ensureUserExists(String username) throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        db.collection("users").document(username).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", username + " Name");
                        userData.put("username", username);
                        userData.put("followers", new ArrayList<String>());
                        userData.put("following", new ArrayList<String>());

                        db.collection("users").document(username).set(userData)
                                .addOnSuccessListener(aVoid -> latch.countDown())
                                .addOnFailureListener(e -> latch.countDown());
                    } else {
                        latch.countDown(); // Already exists
                    }
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await();
    }

    private void ensureFollowRequestExists(String requester, String requested) throws InterruptedException {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        firestore.collection("follow_requests")
                .whereEqualTo("requester", requester)
                .whereEqualTo("requested", requested)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("requester", requester);
                        data.put("requested", requested);
                        data.put("timestamp", System.currentTimeMillis());

                        firestore.collection("follow_requests").add(data)
                                .addOnSuccessListener(docRef -> latch.countDown())
                                .addOnFailureListener(e -> latch.countDown());
                    } else {
                        latch.countDown(); // Already exists
                    }
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await();
    }

    private void assertFollowRequestGone(String requester, String requested) throws InterruptedException {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] exists = {true};

        firestore.collection("follow_requests")
                .whereEqualTo("requester", requester)
                .whereEqualTo("requested", requested)
                .get()
                .addOnSuccessListener(snapshot -> {
                    exists[0] = !snapshot.isEmpty();
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await();

        if (exists[0]) {
            throw new AssertionError("Follow request still exists in Firestore");
        }
    }

    private boolean waitUntilGone(String text, int timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                onView(withText(text)).check(matches(isDisplayed()));
                SystemClock.sleep(500);
            } catch (Exception e) {
                return true; // View not found = success
            }
        }
        return false; // Still there after timeout
    }

    @Test
    public void testIncomingRequest_IsDisplayed() {
        onView(withText("alice wants to follow you.")).check(matches(isDisplayed()));
    }

    @Test
    public void testPendingRequest_IsDisplayed() {
        // Select the second tab (index 1 = "Pending")
        onView(withId(R.id.notifications_tab_layout)).perform(selectTabAtPosition(1));

        // Optional wait for UI to settle (1s max)
        SystemClock.sleep(1000);

        // Assert that the follow request message is shown
        onView(withText("You requested to follow bob. Pending approval."))
                .check(matches(isDisplayed()));
    }


    @Test
    public void testAcceptFollowRequest_RemovesNotification() throws InterruptedException {
        onView(withText("alice wants to follow you.")).check(matches(isDisplayed()));

        onView(allOf(
                withId(R.id.accept_request),
                isDescendantOfA(hasDescendant(withText("alice wants to follow you.")))
        )).perform(click());

        boolean gone = waitUntilGone("alice wants to follow you.", 5000);
        if (!gone) {
            throw new AssertionError("Request still visible after accept");
        }

        assertFollowRequestGone("alice", "testUser");
    }

    @Test
    public void testDenyFollowRequest_RemovesNotification() throws InterruptedException {
        // Add again (just in case it was accepted already)
        ensureFollowRequestExists("alice", "testUser");
        SystemClock.sleep(1000);

        onView(withText("alice wants to follow you.")).check(matches(isDisplayed()));

        // Click Deny
        onView(withId(R.id.deny_request)).perform(click());

        SystemClock.sleep(1500); // wait for UI update

        onView(withText("alice wants to follow you."))
                .check((view, noViewFoundException) -> {
                    if (noViewFoundException == null) {
                        throw new AssertionError("Request still visible after deny");
                    }
                });

        // Check it's removed from Firestore
        assertFollowRequestGone("alice", "testUser");
    }

    private static ViewAction selectTabAtPosition(int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TabLayout.class);
            }

            @Override
            public String getDescription() {
                return "Select tab at index " + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((TabLayout) view).getTabAt(position).select();
            }
        };
    }

    @After
    public void cleanEmulator() {
        String projectId = "moodgram";
        try {
            URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int response = conn.getResponseCode();
            Log.i("TearDown", "Emulator wipe response: " + response);
            conn.disconnect();
        } catch (IOException e) {
            Log.e("TearDown", "Failed to wipe emulator: " + Objects.requireNonNull(e.getMessage()));
        }
    }
}

package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.location.Location;
import android.widget.CheckBox;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.example.superior_intelligence.MoodMap.MoodClusterItem;


/**
 * Test class for MoodMap activity, verifying marker display functionality.
 * Note: The tester must enable location services on their android emulator for the tests to work.
 * Without it, the emulator has no way of determining the user's location.
 */
@RunWith(AndroidJUnit4.class)
public class us060201_060301_060401 {

    @Rule
    public ActivityTestRule<MoodMap> activityRule = new ActivityTestRule<>(MoodMap.class, true, false);

    private FirebaseFirestore mockFirestore;
    private CollectionReference mockMyPostsCollection;
    private Query mockQueryMyPosts;
    private Query mockQueryFollowed;
    private QuerySnapshot mockSnapshotMyPosts;
    private QuerySnapshot mockSnapshotFollowed;

    /**
     * Sets up the test environment before each test method.
     * Initializes the User singleton, Userbase singleton, and mocks for Firestore.
     */
    @Before
    public void setUp() throws Exception {
        // Set up the current user
        User.getInstance().setUsername("testUser");

        // Set up the TestUserbase singleton
        TestUserbase testUserbase = new TestUserbase();
        Field instanceField = Userbase.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, testUserbase);

        // Initialize Firestore mocks
        mockFirestore = mock(FirebaseFirestore.class);
        mockMyPostsCollection = mock(CollectionReference.class);
        mockQueryMyPosts = mock(Query.class);
        mockQueryFollowed = mock(Query.class);
        mockSnapshotMyPosts = mock(QuerySnapshot.class);
        mockSnapshotFollowed = mock(QuerySnapshot.class);

        // Configure mock Firestore behavior
        when(mockFirestore.collection("MyPosts")).thenReturn(mockMyPostsCollection);

        // Mock query for current user's posts
        when(mockMyPostsCollection.whereEqualTo("postUser", "testUser"))
                .thenReturn(mockQueryMyPosts);
        when(mockQueryMyPosts.get()).thenAnswer(invocation -> {
            List<DocumentSnapshot> docs = createMockDocumentsForCurrentUser();
            when(mockSnapshotMyPosts.getDocuments()).thenReturn(docs);
            return Tasks.forResult(mockSnapshotMyPosts);
        });

        // Mock query for followed users' posts
        when(mockMyPostsCollection.whereIn(eq("postUser"), anyList()))
                .thenReturn(mockQueryFollowed);
        when(mockQueryFollowed.get()).thenAnswer(invocation -> {
            List<DocumentSnapshot> docs = createMockDocumentsForFollowedUsers();
            when(mockSnapshotFollowed.getDocuments()).thenReturn(docs);
            return Tasks.forResult(mockSnapshotFollowed);
        });
    }

    /**
     * Cleans up after each test method (if necessary).
     */
    @After
    public void tearDown() {
        // Add cleanup logic here if needed
    }

    /**
     * Tests that markers for the current user's posts are displayed correctly.
     */
    @Test
    public void testMyPostsMarkersDisplayed() throws InterruptedException {
        MoodMap activity = activityRule.launchActivity(new Intent());
        final CountDownLatch latch = new CountDownLatch(1);

        activity.runOnUiThread(() -> {
            try {
                Field dbField = MoodMap.class.getDeclaredField("db");
                dbField.setAccessible(true);
                dbField.set(activity, mockFirestore);

                CheckBox cbMyPosts = activity.findViewById(R.id.cb_myposts);
                cbMyPosts.setChecked(true);
                cbMyPosts.callOnClick(); // trigger internal state change

                activity.markersLatch = latch;
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject mock or set checkbox", e);
            }
        });

        onView(withId(R.id.btn_apply_filters)).perform(click());

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Marker loading timed out", completed);

        final List<MoodMap.MoodClusterItem> clusterItems = new ArrayList<>();
        activity.runOnUiThread(() -> clusterItems.addAll(activity.getClusterItems()));
        Thread.sleep(500); // Optional: slight UI thread buffer

        assertEquals("Expected 3 markers for current user's posts", 3, clusterItems.size());
        String currentUser = User.getInstance().getUsername();
        for (MoodMap.MoodClusterItem item : clusterItems) {
            assertEquals("Marker title should be current user", currentUser, item.getTitle());
            assertTrue("Marker snippet should contain 'Mood:'", item.getSnippet().contains("Mood:"));
        }
    }

    /**
     * Tests that markers for followed users' posts are displayed correctly.
     */
    @Test
    public void testFollowedUsersMarkersDisplayed() throws InterruptedException {
        MoodMap activity = activityRule.launchActivity(new Intent());
        final CountDownLatch latch = new CountDownLatch(1);
        activity.runOnUiThread(() -> {
            try {
                Field dbField = MoodMap.class.getDeclaredField("db");
                dbField.setAccessible(true);
                dbField.set(activity, mockFirestore);

                CheckBox cbMyPosts = activity.findViewById(R.id.cb_myposts);
                cbMyPosts.setChecked(false);

                activity.markersLatch = latch;
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject mock", e);
            }
        });

        onView(withId(R.id.btn_apply_filters)).perform(click());

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Marker loading timed out", completed);

        final List<MoodMap.MoodClusterItem> clusterItems = new ArrayList<>();
        activity.runOnUiThread(() -> clusterItems.addAll(activity.getClusterItems()));
        Thread.sleep(500); // Optional: slight UI thread buffer

        assertEquals("Expected 2 markers for followed users", 2, clusterItems.size());
        String currentUser = User.getInstance().getUsername();
        for (MoodMap.MoodClusterItem item : clusterItems) {
            assertNotEquals("Marker title should not be the current user", currentUser, item.getTitle());
            assertTrue("Marker snippet should contain 'Mood:'", item.getSnippet().contains("Mood:"));
        }
    }

    /**
     * Tests that markers for recent followed users' events within 5 km are displayed correctly.
     */
    @Test
    public void testRecentFollowedUsersEventsWithinRange() throws InterruptedException {
        MoodMap activity = activityRule.launchActivity(new Intent());
        final CountDownLatch latch = new CountDownLatch(1);

        activity.runOnUiThread(() -> {
            try {
                // Inject mock Firestore
                Field dbField = MoodMap.class.getDeclaredField("db");
                dbField.setAccessible(true);
                dbField.set(activity, mockFirestore);

                // Uncheck "My Posts" filter
                CheckBox cbMyPosts = activity.findViewById(R.id.cb_myposts);
                cbMyPosts.setChecked(false);

                // Set the latch for synchronization
                activity.markersLatch = latch;
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject mock or set checkbox", e);
            }
        });

        // Move camera to test center
        activity.runOnUiThread(() -> {
            LatLng testCenter = new LatLng(53.5461, -113.4938);
            activity.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testCenter, 11.5f));
        });

        // Apply filters
        onView(withId(R.id.btn_apply_filters)).perform(click());

        // Wait for async marker clustering to complete
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Marker loading timed out", completed);

        // Retrieve clustered items directly from MoodMap
        final List<MoodClusterItem> clusterItems = activity.getClusterItems();
        assertEquals("Expected 2 markers for recent followed user events within range", 2, clusterItems.size());

        // Confirm they’re within 5km of test center
        Location testCenterLoc = new Location("");
        testCenterLoc.setLatitude(53.5461);
        testCenterLoc.setLongitude(-113.4938);

        for (MoodClusterItem item : clusterItems) {
            Location markerLoc = new Location("");
            markerLoc.setLatitude(item.getPosition().latitude);
            markerLoc.setLongitude(item.getPosition().longitude);
            float distance = testCenterLoc.distanceTo(markerLoc);
            assertTrue("Marker " + item.getTitle() + " should be within 5 km", distance <= 5000);
        }
    }

    /**
     * Creates mock documents for the current user's posts.
     * Returns 3 documents with incremental coordinates and timestamps.
     */
    private List<DocumentSnapshot> createMockDocumentsForCurrentUser() {
        List<DocumentSnapshot> docs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            DocumentSnapshot doc = mock(DocumentSnapshot.class);
            when(doc.getString("postUser")).thenReturn("testUser");
            when(doc.getString("mood")).thenReturn("Happiness");
            when(doc.getDouble("lat")).thenReturn(53.5461 + 0.001 * i);
            when(doc.getDouble("lng")).thenReturn(-113.4938 + 0.001 * i);
            when(doc.getLong("timestamp")).thenReturn(1000L * i);
            docs.add(doc);
        }
        return docs;
    }

    /**
     * Creates mock documents for followed users' posts.
     * Returns 2 documents, one for each followed user.
     */
    private List<DocumentSnapshot> createMockDocumentsForFollowedUsers() {
        List<DocumentSnapshot> docs = new ArrayList<>();

        DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
        when(doc1.getString("postUser")).thenReturn("followed1");
        when(doc1.getString("mood")).thenReturn("Anger");
        when(doc1.getDouble("lat")).thenReturn(53.5461 + 0.002);
        when(doc1.getDouble("lng")).thenReturn(-113.4938 + 0.002);
        when(doc1.getLong("timestamp")).thenReturn(2000L);
        docs.add(doc1);

        DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
        when(doc2.getString("postUser")).thenReturn("followed2");
        when(doc2.getString("mood")).thenReturn("Sadness");
        when(doc2.getDouble("lat")).thenReturn(53.5461 + 0.003);
        when(doc2.getDouble("lng")).thenReturn(-113.4938 + 0.003);
        when(doc2.getLong("timestamp")).thenReturn(3000L);
        docs.add(doc2);

        return docs;
    }

    /**
     * Test implementation of Userbase that returns a fixed list of followed users.
     */
    public static class TestUserbase extends Userbase {
        @Override
        public void getUserFollowing(String username, UserListCallback callback) {
            callback.onUserListRetrieved(Arrays.asList("followed1", "followed2"));
        }
    }

    private Map<String, Object> createMockDoc(String username, double lat, double lng, String mood) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("username", username);
        doc.put("mood", mood);
        doc.put("reason", "testing");
        doc.put("timestamp", new Timestamp(new Date())); // recent date
        doc.put("location", new GeoPoint(lat, lng));
        return doc;
    }

}
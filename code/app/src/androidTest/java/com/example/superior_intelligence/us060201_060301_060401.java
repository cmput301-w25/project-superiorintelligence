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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * UI test class for verifying the behavior of the MoodMap activity.
 * This class tests whether mood markers for the current user and followed users
 * are displayed correctly on the map.
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
     * Sets up the test environment by initializing mock Firestore instances
     * and replacing the Userbase instance with a test double.
     *
     * @throws Exception if an error occurs while setting up reflection for Userbase
     */
    @Before
    public void setUp() throws Exception {
        User.getInstance().setUsername("testUser");

        // Replace Userbase with test double
        Field instanceField = Userbase.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, new TestUserbase());

        mockFirestore = mock(FirebaseFirestore.class);
        mockMyPostsCollection = mock(CollectionReference.class);
        mockQueryMyPosts = mock(Query.class);
        mockQueryFollowed = mock(Query.class);
        mockSnapshotMyPosts = mock(QuerySnapshot.class);
        mockSnapshotFollowed = mock(QuerySnapshot.class);

        when(mockFirestore.collection("MyPosts")).thenReturn(mockMyPostsCollection);

        when(mockMyPostsCollection.whereEqualTo("postUser", "testUser")).thenReturn(mockQueryMyPosts);
        when(mockQueryMyPosts.get()).thenAnswer(invocation -> {
            when(mockSnapshotMyPosts.getDocuments()).thenReturn(createMockDocumentsForCurrentUser());
            return Tasks.forResult(mockSnapshotMyPosts);
        });

        when(mockMyPostsCollection.whereIn(eq("postUser"), anyList())).thenReturn(mockQueryFollowed);
        when(mockQueryFollowed.get()).thenAnswer(invocation -> {
            when(mockSnapshotFollowed.getDocuments()).thenReturn(createMockDocumentsForFollowedUsers());
            return Tasks.forResult(mockSnapshotFollowed);
        });
    }

    /**
     * Cleans up the test environment by resetting MoodMap test configurations.
     */
    @After
    public void tearDown() {
        MoodMap.testMode = false;
        MoodMap.injectedFirestore = null;
    }

    /**
     * Tests if markers for the current user's posts are correctly displayed on the map.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for markers to load
     */
    @Test
    public void testMyPostsMarkersDisplayed() throws InterruptedException {
        MoodMap.testMode = true;
        MoodMap.injectedFirestore = mockFirestore;

        MoodMap activity = activityRule.launchActivity(new Intent());
        final CountDownLatch latch = new CountDownLatch(1);

        activity.runOnUiThread(() -> {
            CheckBox cbMyPosts = activity.findViewById(R.id.cb_myposts);
            cbMyPosts.setChecked(true);
            activity.markersLatch = latch;
            activity.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(53.5461, -113.4938), 11.5f));
            activity.applyFilters();
        });

        assertTrue("Marker loading timed out", latch.await(5, TimeUnit.SECONDS));

        final List<Marker> markers = new ArrayList<>();
        activity.runOnUiThread(() -> markers.addAll(activity.getMarkers()));

        assertEquals("Expected 3 markers for current user's posts", 3, markers.size());
        String currentUser = User.getInstance().getUsername();
        for (Marker marker : markers) {
            assertEquals(currentUser, marker.getTitle());
            assertTrue(marker.getSnippet().contains("Mood:"));
        }
    }

    /**
     * Tests if markers for followed users' posts are correctly displayed on the map.
     *  @throws InterruptedException if the thread is interrupted while waiting for markers to load
     */
    @Test
    public void testFollowedUsersMarkersDisplayed() throws InterruptedException {
        MoodMap.testMode = true;
        MoodMap.injectedFirestore = mockFirestore;

        MoodMap activity = activityRule.launchActivity(new Intent());
        final CountDownLatch latch = new CountDownLatch(1);

        activity.runOnUiThread(() -> {
            CheckBox cbMyPosts = activity.findViewById(R.id.cb_myposts);
            cbMyPosts.setChecked(false);
            activity.markersLatch = latch;
            activity.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(53.5461, -113.4938), 11.5f));
            activity.applyFilters();
        });

        assertTrue("Marker loading timed out", latch.await(5, TimeUnit.SECONDS));

        final List<Marker> markers = new ArrayList<>();
        activity.runOnUiThread(() -> markers.addAll(activity.getMarkers()));

        assertEquals("Expected 2 markers for followed users", 2, markers.size());
        String currentUser = User.getInstance().getUsername();
        for (Marker marker : markers) {
            assertNotEquals(currentUser, marker.getTitle());
            assertTrue(marker.getSnippet().contains("Mood:"));
        }
    }

    /**
     * Tests if recent events from followed users are within a 5 km range of the map center.
     *  @throws InterruptedException if the thread is interrupted while waiting for markers to load
     */
    @Test
    public void testRecentFollowedUsersEventsWithinRange() throws InterruptedException {
        MoodMap.testMode = true;
        MoodMap.injectedFirestore = mockFirestore;

        MoodMap activity = activityRule.launchActivity(new Intent());
        final CountDownLatch latch = new CountDownLatch(1);

        activity.runOnUiThread(() -> {
            CheckBox cbMyPosts = activity.findViewById(R.id.cb_myposts);
            cbMyPosts.setChecked(false);
            activity.markersLatch = latch;

            // Center map on Edmonton
            LatLng testCenter = new LatLng(53.5461, -113.4938);
            activity.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testCenter, 11.5f));

            activity.applyFilters();
        });

        assertTrue("Marker loading timed out", latch.await(5, TimeUnit.SECONDS));

        final List<Marker> markers = new ArrayList<>();
        activity.runOnUiThread(() -> markers.addAll(activity.getMarkers()));

        assertEquals("Expected 2 markers within range", 2, markers.size());

        Location testCenterLoc = new Location("");
        testCenterLoc.setLatitude(53.5461);
        testCenterLoc.setLongitude(-113.4938);
        for (Marker marker : markers) {
            Location markerLoc = new Location("");
            markerLoc.setLatitude(marker.getPosition().latitude);
            markerLoc.setLongitude(marker.getPosition().longitude);
            float distance = testCenterLoc.distanceTo(markerLoc);
            assertTrue("Marker " + marker.getTitle() + " should be within 5 km", distance <= 5000);
        }
    }

    /**
     * Mocks Firestore documents for the current user's posts.
     *
     * @return a list of mocked Firestore documents representing the current user's posts
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
     * Mocks Firestore documents for followed users' posts.
     *
     * @return a list of mocked Firestore documents representing followed users' posts
     */
    private List<DocumentSnapshot> createMockDocumentsForFollowedUsers() {
        List<DocumentSnapshot> docs = new ArrayList<>();

        DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
        when(doc1.getString("postUser")).thenReturn("followed1");
        when(doc1.getString("mood")).thenReturn("Anger");
        when(doc1.getDouble("lat")).thenReturn(53.5481);
        when(doc1.getDouble("lng")).thenReturn(-113.4918);
        when(doc1.getLong("timestamp")).thenReturn(2000L);
        docs.add(doc1);

        DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
        when(doc2.getString("postUser")).thenReturn("followed2");
        when(doc2.getString("mood")).thenReturn("Sadness");
        when(doc2.getDouble("lat")).thenReturn(53.5491);
        when(doc2.getDouble("lng")).thenReturn(-113.4908);
        when(doc2.getLong("timestamp")).thenReturn(3000L);
        docs.add(doc2);

        return docs;
    }

    /**
     * Test double for Userbase that provides a fixed list of followed users.
     */
    public static class TestUserbase extends Userbase {
        /**
         * Provides a fixed list of followed users for testing purposes.
         *
         * @param username the username of the current user
         * @param callback the callback to receive the list of followed users
         */
        @Override
        public void getUserFollowing(String username, UserListCallback callback) {
            callback.onUserListRetrieved(Arrays.asList("followed1", "followed2"));
        }
    }
}

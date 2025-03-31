package com.example.superior_intelligence;

import android.location.Location;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * UI tests for MoodMap activity covering:
 * US 06.02.01, US 06.03.01, and US 06.04.01.
 *
 * Preconditions:
 * 1. MoodMap activity is instrumented to expose a public method getMarkers() that returns a List<Marker>
 *    representing the markers currently loaded on the GoogleMap.
 * 2. A fake/test Firestore is injected so that queries return predictable mood events for the current user and followed users.
 * 3. The current user (via User.getInstance()) and the followed users are set up with known data.
 */
@RunWith(AndroidJUnit4.class)
public class us060201_060301_060401 {

    @Rule
    public ActivityTestRule<MoodMap> activityRule = new ActivityTestRule<>(MoodMap.class);

    // If asynchronous Firebase calls are used, register an IdlingResource.
    private IdlingResource firebaseIdlingResource;

    @Before
    public void registerIdlingResource() {
        // If you have an IdlingResource for Firebase calls, register it here.
        // Example:
        // firebaseIdlingResource = FirebaseIdlingResource.getInstance();
        // IdlingRegistry.getInstance().register(firebaseIdlingResource);
    }

    @After
    public void unregisterIdlingResource() {
        if (firebaseIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(firebaseIdlingResource);
        }
    }

    /**
     * Test US 06.02.01:
     * When "My Posts" is checked, all mood events from the current user (with valid locations and within 5 km)
     * are displayed on the map.
     */
    @Test
    public void testMyPostsMarkersDisplayed() {
        // Ensure "My Posts" is checked.
        onView(withId(R.id.cb_myposts)).perform(click());

        // Optionally, select additional mood filters as needed.
        // For instance: onView(withId(R.id.cb_happy)).perform(click());

        // Apply filters.
        onView(withId(R.id.btn_apply_filters)).perform(click());

        // Get the activity and retrieve markers using the test hook.
        MoodMap activity = activityRule.getActivity();
        // Assuming getMarkers() returns a List<Marker> containing all markers currently added.
        List<Marker> markers = activity.getMarkers();

        // For the test environment, assume the fake Firestore returns 3 mood events for the current user.
        assertEquals("Expected 3 markers for current user's posts", 3, markers.size());

        // Verify that each marker displays the current user's username and mood information.
        String currentUser = User.getInstance().getUsername();
        for (Marker marker : markers) {
            // The marker title should match the current user's username.
            assertEquals("Marker title should be current user", currentUser, marker.getTitle());
            // The snippet should contain the text "Mood:" indicating mood information.
            assertTrue("Marker snippet should contain 'Mood:'", marker.getSnippet().contains("Mood:"));
        }
    }

    /**
     * Test US 06.03.01:
     * When "My Posts" is unchecked, the map displays only the most recent mood event
     * for each followed user, with markers showing the username and mood.
     */
    @Test
    public void testFollowedUsersMarkersDisplayed() {
        // Ensure "My Posts" is unchecked (toggle if it is checked by default).
        onView(withId(R.id.cb_myposts)).perform(click());

        // Apply filters.
        onView(withId(R.id.btn_apply_filters)).perform(click());

        // Get the markers from the activity.
        MoodMap activity = activityRule.getActivity();
        List<Marker> markers = activity.getMarkers();

        // For the test environment, assume there are 2 followed users with valid events.
        assertEquals("Expected 2 markers for followed users", 2, markers.size());

        String currentUser = User.getInstance().getUsername();
        for (Marker marker : markers) {
            // Verify that the marker title is not the current user's username.
            assertNotEquals("Marker title should not be the current user", currentUser, marker.getTitle());
            // Verify that the marker snippet contains mood information.
            assertTrue("Marker snippet should contain 'Mood:'", marker.getSnippet().contains("Mood:"));
        }
    }

    /**
     * Test US 06.04.01:
     * Only the most recent mood event of every followed user is shown,
     * and each event is within 5 km of the current map center.
     */
    @Test
    public void testRecentFollowedUsersEventsWithinRange() {
        // Ensure "My Posts" is unchecked.
        onView(withId(R.id.cb_myposts)).perform(click());

        // Adjust the map camera to a test location known to have events within range.
        MoodMap activity = activityRule.getActivity();
        activity.runOnUiThread(() -> {
            // Using Edmonton's coordinates as an example.
            LatLng testCenter = new LatLng(53.5461, -113.4938);
            activity.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testCenter, 11.5f));
        });

        // Apply filters.
        onView(withId(R.id.btn_apply_filters)).perform(click());

        // Retrieve markers.
        List<Marker> markers = activity.getMarkers();

        // For the test environment, assume the fake Firestore returns 2 events from followed users.
        assertEquals("Expected 2 markers for recent followed user events within range", 2, markers.size());

        // Verify each marker is within 5 km of the test center.
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
}

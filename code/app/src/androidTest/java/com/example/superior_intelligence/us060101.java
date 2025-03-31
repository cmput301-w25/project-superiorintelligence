package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.core.content.ContextCompat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI test for US 06.01.01:
 * "As a participant, I want to optionally attach my current location to a mood event."
 *
 * Preconditions:
 * 1. MoodCreateAndEditActivity includes test hooks getLat() and getLng().
 * 2. The test environment automatically grants location permission.
 *
 * This test verifies that:
 * - When the "include_map_checkbox" is checked, the activity retrieves a nonzero location.
 * - When the checkbox is unchecked, the latitude and longitude are reset to 0.0.
 */
@RunWith(AndroidJUnit4.class)
public class us060101 {

    // Automatically grant location permission for testing.
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void testAttachAndDetachLocation() throws InterruptedException {
        // Launch the activity.
        ActivityScenario<MoodCreateAndEditActivity> scenario = ActivityScenario.launch(MoodCreateAndEditActivity.class);

        // Pre-condition: Verify that permission is granted.
        scenario.onActivity(activity -> {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Test requires location permission to be granted.");
            }
        });

        // Check the "include_map_checkbox" to trigger location retrieval.
        onView(withId(R.id.include_map_checkbox)).perform(click());

        // Wait briefly for asynchronous location retrieval.
        Thread.sleep(2000);

        // Verify that location values are nonzero.
        scenario.onActivity(activity -> {
            double retrievedLat = activity.getLat();
            double retrievedLng = activity.getLng();

            // Assert that location has been attached.
            assertNotEquals("Latitude should not be 0.0 after attaching location", 0.0, retrievedLat, 0.0);
            assertNotEquals("Longitude should not be 0.0 after attaching location", 0.0, retrievedLng, 0.0);
        });

        // Uncheck the checkbox to remove the location.
        onView(withId(R.id.include_map_checkbox)).perform(click());

        // Wait a moment for UI update.
        Thread.sleep(500);

        // Verify that location values are reset.
        scenario.onActivity(activity -> {
            double resetLat = activity.getLat();
            double resetLng = activity.getLng();

            assertEquals("Latitude should be reset to 0.0 after unchecking location", 0.0, resetLat, 0.0);
            assertEquals("Longitude should be reset to 0.0 after unchecking location", 0.0, resetLng, 0.0);
        });
    }
}

package com.example.superior_intelligence;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for non-UI logic in MoodMap.java.
 * These tests validate:
 * - Distance calculation between coordinates (Haversine formula)
 * - Whether a point is within a given radius
 * - Mood-to-icon mapping logic
 */
public class TestMoodMap {

    /**
     * Tests that the distance between two close points is below 0.5 km.
     */
    @Test
    public void testDistanceInKilometers_betweenNearbyPoints() {
        double lat1 = 53.5461;
        double lon1 = -113.4938;
        double lat2 = 53.5471;
        double lon2 = -113.4928;

        float distance = MoodMap.distanceInKilometers(lat1, lon1, lat2, lon2);
        assertTrue("Expected distance < 0.5 km", distance < 0.5f);
    }

    /**
     * Verifies that two coordinates within a 5 km radius return true.
     */
    @Test
    public void testIsWithinRange_trueCase() {
        double lat1 = 53.5461;
        double lon1 = -113.4938;
        double lat2 = 53.5471;
        double lon2 = -113.4928;

        assertTrue("Expected to be within 5 km",
                MoodMap.isWithinRange(lat1, lon1, lat2, lon2, 5.0f));
    }

    /**
     * Verifies that coordinates beyond a 5 km radius return false.
     */
    @Test
    public void testIsWithinRange_falseCase() {
        double lat1 = 53.5461;
        double lon1 = -113.4938;
        double lat2 = 53.6000;
        double lon2 = -113.5000;

        assertFalse("Expected to be outside 5 km",
                MoodMap.isWithinRange(lat1, lon1, lat2, lon2, 5.0f));
    }

    /**
     * Checks that valid moods return a non-error drawable ID.
     */
    @Test
    public void testGetMoodMarkerIcon_validMoodsReturnDrawableId() {
        assertNotEquals(-1, MoodMap.getMoodMarkerIcon("anger"));
        assertNotEquals(-1, MoodMap.getMoodMarkerIcon("happiness"));
        assertNotEquals(-1, MoodMap.getMoodMarkerIcon("confusion"));
        assertNotEquals(-1, MoodMap.getMoodMarkerIcon("shame"));
    }

    /**
     * Ensures invalid or null mood strings return -1.
     */
    @Test
    public void testGetMoodMarkerIcon_invalidMoodReturnsMinusOne() {
        assertEquals(-1, MoodMap.getMoodMarkerIcon("joyful")); // Not defined
        assertEquals(-1, MoodMap.getMoodMarkerIcon(""));       // Empty string
        assertEquals(-1, MoodMap.getMoodMarkerIcon(null));     // Null value
    }
}

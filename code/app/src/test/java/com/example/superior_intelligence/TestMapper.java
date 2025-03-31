package com.example.superior_intelligence;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.Map;

public class TestMapper {

    private Event mockEvent;

    /**
     * Sets up a mock Event object before each test for use in event-to-map conversion.
     */
    @Before
    public void setUp() {
        mockEvent = new Event(
                "12345",
                "Test Event",
                "28 Mar 2025",
                "#FFD700",
                "test/image/url",
                101,
                true,
                true,
                "happiness",
                "Feeling good",
                "Alone",
                "test_user",
                53.5461,
                -113.4938,
                true
        );
    }

    /**
     * Makes sure that eventToMap() converts an Event into a Map
     * with the right values for every field.
     */
    @Test
    public void testEventToMap() {
        Map<String, Object> result = Mapper.eventToMap(mockEvent);

        assertEquals("12345", result.get("id"));
        assertEquals("Test Event", result.get("title"));
        assertEquals("28 Mar 2025", result.get("date"));
        assertEquals("#FFD700", result.get("overlayColor"));
        assertEquals("test/image/url", result.get("imageUrl"));
        assertEquals(101, result.get("emojiResource"));
        assertTrue((Boolean) result.get("isFollowed"));
        assertTrue((Boolean) result.get("isMyPost"));
        assertEquals("happiness", result.get("mood"));
        assertEquals("Feeling good", result.get("moodExplanation"));
        assertEquals("Alone", result.get("situation"));
        assertEquals("test_user", result.get("postUser"));
        assertEquals(53.5461, result.get("lat"));
        assertEquals(-113.4938, result.get("lng"));
        assertTrue((Boolean) result.get("public_status"));
    }

    /**
     * Checks that docToEvent() builds a proper Event object from a mocked Firestore document.
     * This test includes a Timestamp date to make sure formatting works too.
     */
    @Test
    public void testDocToEvent() {
        DocumentSnapshot mockSnapshot = Mockito.mock(DocumentSnapshot.class);

        // Set date to 28 Mar 2025 at 00:00 local time
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MARCH, 28, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp timestamp = new Timestamp(calendar.getTime());

        // Mock Firestore document fields
        Mockito.when(mockSnapshot.getString("id")).thenReturn("12345");
        Mockito.when(mockSnapshot.getString("title")).thenReturn("Mock Event");
        Mockito.when(mockSnapshot.get("date")).thenReturn(timestamp);
        Mockito.when(mockSnapshot.getString("overlayColor")).thenReturn("#87CEEB");
        Mockito.when(mockSnapshot.getString("imageUrl")).thenReturn("mock/image/url");
        Mockito.when(mockSnapshot.getLong("emojiResource")).thenReturn(102L);
        Mockito.when(mockSnapshot.getBoolean("isFollowed")).thenReturn(false);
        Mockito.when(mockSnapshot.getBoolean("isMyPost")).thenReturn(true);
        Mockito.when(mockSnapshot.getString("mood")).thenReturn("sadness");
        Mockito.when(mockSnapshot.getString("moodExplanation")).thenReturn("Bad day");
        Mockito.when(mockSnapshot.getString("situation")).thenReturn("With friends");
        Mockito.when(mockSnapshot.getString("postUser")).thenReturn("mock_user");
        Mockito.when(mockSnapshot.getDouble("lat")).thenReturn(45.4215);
        Mockito.when(mockSnapshot.getDouble("lng")).thenReturn(-75.6999);
        Mockito.when(mockSnapshot.getBoolean("public_status")).thenReturn(false);

        Event result = Mapper.docToEvent(mockSnapshot);

        assertEquals("12345", result.getID());
        assertEquals("Mock Event", result.getTitle());
        assertEquals("28 Mar 2025", result.getDate());
        assertEquals("#87CEEB", result.getOverlayColor());
        assertEquals("mock/image/url", result.getImageUrl());
        assertEquals(102, result.getEmojiResource());
        assertFalse(result.isFollowed());
        assertTrue(result.isMyPost());
        assertEquals("sadness", result.getMood());
        assertEquals("Bad day", result.getMoodExplanation());
        assertEquals("With friends", result.getSituation());
        assertEquals("mock_user", result.getPostUser());
        assertEquals(45.4215, result.getLat(), 0.0001);
        assertEquals(-75.6999, result.getLng(), 0.0001);
        assertFalse(result.isPublic_status());
    }
}

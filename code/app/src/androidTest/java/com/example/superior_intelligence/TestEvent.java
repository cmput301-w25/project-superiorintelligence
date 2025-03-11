package com.example.superior_intelligence;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestEvent {

    private Event event;
    private Timestamp testTimestamp;

    /**
     * Sets up test data before each test case runs.
     */
    @Before
    public void setUp() {
        testTimestamp = new Timestamp(new Date());
        event = new Event("Test Title", "10 Mar 2024", "#FF0000", "http://example.com/image.jpg",
                123, true, true, "Happiness", "Feeling great!", "With friends", "testuser", (double) 0, (double) 0);
    }

    /**
     * Test Constructor with Full Parameters.
     */
    @Test
    public void testEventConstructor() {
        assertEquals("Test Title", event.getTitle());
        assertEquals("10 Mar 2024", event.getDate());
        assertEquals("#FF0000", event.getOverlayColor());
        assertEquals("http://example.com/image.jpg", event.getImageUrl());
        assertEquals(123, event.getEmojiResource());
        assertTrue(event.isFollowed());
        assertTrue(event.isMyPost());
        assertEquals("Happiness", event.getMood());
        assertEquals("Feeling great!", event.getMoodExplanation());
        assertEquals("With friends", event.getSituation());
        assertEquals("testuser", event.getUser());
    }

    /**
     * Test Getter for `getDate()` when the date is a string.
     */
    @Test
    public void testGetDateAsString() {
        assertEquals("10 Mar 2024", event.getDate());
    }

    /**
     * Test Setters and Getters for Event Properties.
     */
    @Test
    public void testSettersAndGetters() {
        event.setTitle("Updated Title");
        assertEquals("Updated Title", event.getTitle());

        event.setDate("11 Mar 2024");
        assertEquals("11 Mar 2024", event.getDate());

        event.setOverlayColor("#00FF00");
        assertEquals("#00FF00", event.getOverlayColor());

        event.setImageUrl("http://newimage.com/img.jpg");
        assertEquals("http://newimage.com/img.jpg", event.getImageUrl());

        event.setEmojiResource(456);
        assertEquals(456, event.getEmojiResource());

        event.setFollowed(false);
        assertFalse(event.isFollowed());

        event.setMyPost(false);
        assertFalse(event.isMyPost());

        event.setMood("Sadness");
        assertEquals("Sadness", event.getMood());

        event.setMoodExplanation("Not feeling well.");
        assertEquals("Not feeling well.", event.getMoodExplanation());

        event.setSituation("Alone");
        assertEquals("Alone", event.getSituation());

        event.setUser("newuser");
        assertEquals("newuser", event.getUser());
    }

    /**
     * Test Empty Constructor (Firestore deserialization test).
     */
    @Test
    public void testEmptyConstructor() {
        Event emptyEvent = new Event();
        assertNotNull(emptyEvent);
    }

    /**
     * Test Edge Cases: Null and Empty Values.
     */
    @Test
    public void testEdgeCases() {
        event.setTitle(null);
        assertNull(event.getTitle());

        event.setMoodExplanation("");
        assertEquals("", event.getMoodExplanation());

        event.setSituation(null);
        assertNull(event.getSituation());
    }
}

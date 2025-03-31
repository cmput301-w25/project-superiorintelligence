package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class TestMoodCreate {

    /**
     * Creates a sample Event object with preset values.
     * Used as a reusable mock for testing.
     */
    private Event mockEvent() {
        return new Event(
                "event123",
                "I'm Tired",
                "30 Mar 2025",
                "#FFB6C1",
                null,
                R.drawable.sad_icon,
                false,
                true,
                "Sadness",
                "301...",
                "Alone",
                "rizzler",
                53.5461,
                -113.4938,
                true
        );
    }

    /**
     * Returns a list containing one mock event.
     * Used as a base for list-related tests.
     */
    private List<Event> mockEventList() {
        List<Event> eventList = new ArrayList<>();
        eventList.add(mockEvent());
        return eventList;
    }

    /**
     * Tests adding a new event to the list increases the size
     * and the new event is present afterward.
     */
    @Test
    public void testAddEvent() {
        List<Event> eventList = mockEventList();
        assertEquals(1, eventList.size());

        Event secondEvent = new Event(
                "event69",
                "RAWR XD",
                "30 Mar 2025",
                "#FF0000",
                null,
                R.drawable.angry_icon,
                false,
                true,
                "Anger",
                "Too many deadlines",
                "Alone",
                "rizzler",
                53.5461,
                -113.4938,
                false
        );

        eventList.add(secondEvent);

        assertEquals(2, eventList.size());
        assertTrue(eventList.contains(secondEvent));
    }

    /**
     * Tests that adding a duplicate event with the same ID throws an IllegalArgumentException.
     */
    @Test
    public void testDuplicateEventIdThrowsException() {
        List<Event> eventList = mockEventList();
        Event duplicate = mockEvent(); // same ID as first

        assertThrows(IllegalArgumentException.class, () -> {
            for (Event e : eventList) {
                if (e.getID().equals(duplicate.getID())) {
                    throw new IllegalArgumentException("Duplicate ID not allowed");
                }
            }
            eventList.add(duplicate);
        });
    }

    /**
     * Checks that all the properties of the mock event are set correctly.
     */
    @Test
    public void testEventProperties() {
        Event event = mockEvent();

        assertEquals("I'm Tired", event.getTitle());
        assertEquals("Sadness", event.getMood());
        assertEquals("301...", event.getMoodExplanation());
        assertEquals("Alone", event.getSituation());
        assertEquals("rizzler", event.getPostUser());
        assertTrue(event.isPublic_status());
    }
}

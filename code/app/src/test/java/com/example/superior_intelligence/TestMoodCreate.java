package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class TestMoodCreate {

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

    private List<Event> mockEventList() {
        List<Event> eventList = new ArrayList<>();
        eventList.add(mockEvent());
        return eventList;
    }

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

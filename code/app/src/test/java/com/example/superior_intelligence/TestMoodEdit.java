package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestMoodEdit {

    /**
     * Returns a mock Event that represents the original (pre-edited) state.
     */
    private Event mockOriginalEvent() {
        return new Event(
                "edit123",
                "Old Mood",
                "30 Mar 2025",
                "#87CEEB",
                null,
                R.drawable.sad_icon,
                false,
                true,
                "Sadness",
                "Midterms suck",
                "Alone",
                "rizzler",
                53.5,
                -113.4,
                true
        );
    }

    /**
     * Returns a mock Event that simulates the updated version of the original event.
     */
    private Event mockUpdatedEvent() {
        return new Event(
                "edit123",
                "New Mood",
                "30 Mar 2025",
                "#FF6347",
                null,
                R.drawable.angry_icon,
                false,
                true,
                "Anger",
                "Someone ate my lunch",
                "With Friends",
                "rizzler",
                53.5,
                -113.4,
                false
        );
    }

    /**
     * Returns a list with one editable mock event inside.
     */
    private List<Event> mockEventListWithEditable() {
        List<Event> events = new ArrayList<>();
        events.add(mockOriginalEvent());
        return events;
    }

    /**
     * Tests that editing an existing event by ID correctly replaces it in the list.
     */
    @Test
    public void testEditEventReplacesOldOne() {
        List<Event> eventList = mockEventListWithEditable();
        Event updated = mockUpdatedEvent();

        boolean replaced = false;
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getID().equals(updated.getID())) {
                eventList.set(i, updated);
                replaced = true;
                break;
            }
        }

        assertEquals(1, eventList.size());
        assertTrue(replaced);
        assertEquals("New Mood", eventList.get(0).getTitle());
        assertEquals("Anger", eventList.get(0).getMood());
        assertEquals("With Friends", eventList.get(0).getSituation());
        assertEquals("Someone ate my lunch", eventList.get(0).getMoodExplanation());
        assertEquals(false, eventList.get(0).isPublic_status());
    }

    /**
     * Tests that trying to edit an event with a non-matching ID throws an exception.
     */
    @Test
    public void testEditFailsIfIdMismatch() {
        List<Event> events = mockEventListWithEditable();
        Event nonMatching = new Event(
                "not_editing_this_one",
                "Wrong Edit",
                "30 Mar 2025",
                "#FFFFFF",
                null,
                R.drawable.happy_icon,
                false,
                true,
                "Happiness",
                "Nice weather",
                "Alone",
                "rizzler",
                0.0,
                0.0,
                true
        );

        assertThrows(IllegalArgumentException.class, () -> {
            boolean found = false;
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getID().equals(nonMatching.getID())) {
                    events.set(i, nonMatching);
                    found = true;
                    break;
                }
            }
            if (!found) throw new IllegalArgumentException("Event ID not found for editing");
        });
    }

    /**
     * Confirms that the original event and updated event differ in all the important fields.
     */
    @Test
    public void testOriginalAndEditedEventsDiffer() {
        Event original = mockOriginalEvent();
        Event updated = mockUpdatedEvent();

        assertNotEquals(original.getTitle(), updated.getTitle());
        assertNotEquals(original.getMood(), updated.getMood());
        assertNotEquals(original.getMoodExplanation(), updated.getMoodExplanation());
        assertNotEquals(original.getSituation(), updated.getSituation());
        assertNotEquals(original.isPublic_status(), updated.isPublic_status());
    }
}
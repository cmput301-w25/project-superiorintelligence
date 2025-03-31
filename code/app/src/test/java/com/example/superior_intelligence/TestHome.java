package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TestHome {

    private List<Event> mockEvents;

    @Before
    public void setUp() {
        mockEvents = new ArrayList<>();

        // Create 3 events: 2 Shame, 1 Happy
        Event event1 = new Event();
        event1.setMoodExplanation("Job interview!");
        event1.setMood("Happiness");
        event1.setTimestamp(System.currentTimeMillis());
        event1.setDate(getDateOffsetFromToday(0)); // today

        Event event2 = new Event();
        event2.setMoodExplanation("Ghosted again...");
        event2.setMood("Anger");
        event2.setTimestamp(System.currentTimeMillis() - 100000);
        event2.setDate(getDateOffsetFromToday(-3)); // 3 days ago

        Event event3 = new Event();
        event3.setMoodExplanation("I hit a person with my car today...");
        event3.setMood("Shame");
        event3.setTimestamp(System.currentTimeMillis() - 200000);
        event3.setDate(getDateOffsetFromToday(-10)); // 10 days ago (outside recent week)

        mockEvents.add(event1);
        mockEvents.add(event2);
        mockEvents.add(event3);
    }

    @Test
    public void testFilterByReason() {
        List<Event> result = HomeManager.filterByReason("car", mockEvents);

        assertEquals(1, result.size());
        for (Event e : result) {
            assertTrue(e.getMoodExplanation().toLowerCase().contains("car"));
        }
    }


    @Test
    public void testFilterByReason_noMatchReturnsEmpty() {
        List<Event> result = HomeManager.filterByReason("thiswordisnotreal", mockEvents);
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterByMood_shameOnly() {
        List<String> moodList = Arrays.asList("Anger");

        List<Event> result = HomeManager.filterByMood(moodList, mockEvents);

        assertEquals(1, result.size());
        for (Event e : result) {
            assertEquals("Anger", e.getMood());
        }
    }

    @Test
    public void testFilterRecentWeek_onlyEventsFromLast7Days() {
        List<Event> result = HomeManager.filterRecentWeek(mockEvents);

        assertEquals(2, result.size());
        for (Event e : result) {
            assertTrue(e.getDate().contains("202")); // crude check it didn't fail date parsing
        }
    }

    @Test
    public void testRecentThree_returnsLatestEvents() {
        List<Event> result = HomeManager.recentThree(mockEvents);

        assertEquals(3, result.size());
        assertEquals(mockEvents.get(0), result.get(0)); // newest event first
    }

    // Helper to get formatted date string "dd MMM yyyy, HH:mm"
    private String getDateOffsetFromToday(int daysOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysOffset);
        return new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm").format(cal.getTime());
    }

    @Test
    public void testUpsertEvent_addsNewEventIfNotFound() {
        List<Event> events = new ArrayList<>();
        Event e1 = new Event();
        e1.setID("abc123");
        events.add(e1);

        Event newEvent = new Event();
        newEvent.setID("def456");

        HomeManager.upsertEvent(events, newEvent);

        assertEquals(2, events.size());
        assertTrue(events.contains(newEvent));
    }

    @Test
    public void testUpsertEvent_updatesExistingEvent() {
        Event existing = new Event();
        existing.setID("abc123");
        existing.setMoodExplanation("Old reason");

        List<Event> events = new ArrayList<>();
        events.add(existing);

        Event updated = new Event();
        updated.setID("abc123");
        updated.setMoodExplanation("Updated reason");

        HomeManager.upsertEvent(events, updated);

        assertEquals(1, events.size());
        assertEquals("Updated reason", events.get(0).getMoodExplanation());
    }

    @Test
    public void testRemoveEventById_removesMatchingEvent() {
        Event e1 = new Event();
        e1.setID("abc123");

        Event e2 = new Event();
        e2.setID("def456");

        List<Event> events = new ArrayList<>();
        events.add(e1);
        events.add(e2);

        HomeManager.removeEventById(events, "abc123");

        assertEquals(1, events.size());
        assertEquals("def456", events.get(0).getID());
    }
}

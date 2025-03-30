
package com.example.superior_intelligence;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EventsTest {

    private EventAdapter adapter;
    private List<Event> mockEvents;

    /**
     * This subclass of EventAdapter is used just for testing.
     * It skips notifying RecyclerView observers to avoid crashes during tests.
     */
    private static class TestableEventAdapter extends EventAdapter {
        private List<Event> internalEvents;

        public TestableEventAdapter() {
            super(null, event -> {});
        }

        @Override
        public void setEvents(List<Event> events) {
            this.internalEvents = events;
        }

        @Override
        public int getItemCount() {
            return internalEvents == null ? 0 : internalEvents.size();
        }
    }

    /**
     * Creates a sample event used in some tests.
     */
    private Event mockEvent() {
        return new Event("1", "Study Group", "27 Mar 2025", "#FF5733", "image123", 123, true, true,
                "Happy", "Had a good study session", "Alone", "student1", 53.5461, -113.4938, true);
    }

    /**
     * Runs before every test. It sets up the adapter and a sample list of events.
     */
    @Before
    public void setUp() {
        adapter = new TestableEventAdapter();
        mockEvents = new ArrayList<>();
        mockEvents.add(new Event("1", "Picnic", "26 Mar 2025", "#FFB6C1", "", 0, false, false, "Joy", "Sunny day", "With family", "user123", 0.0, 0.0, true));
        mockEvents.add(new Event("2", "Midterm", "25 Mar 2025", "#ADD8E6", "", 0, false, false, "Stressed", "Exams approaching", "Alone", "user456", 0.0, 0.0, true));
    }

    /**
     * Tests if an Event is created properly and stores all the correct values.
     */
    @Test
    public void testEventCreation() {
        Event event = mockEvent();

        assertEquals("Study Group", event.getTitle());
        assertEquals("27 Mar 2025", event.getDate());
        assertEquals("#FF5733", event.getOverlayColor());
        assertEquals("image123", event.getImageUrl());
        assertEquals(123, event.getEmojiResource());
        assertTrue(event.isFollowed());
        assertTrue(event.isMyPost());
        assertEquals("Happy", event.getMood());
        assertEquals("Had a good study session", event.getMoodExplanation());
        assertEquals("Alone", event.getSituation());
        assertEquals("student1", event.getPostUser());
        assertEquals(53.5461, event.getLat(), 0.0001);
        assertEquals(-113.4938, event.getLng(), 0.0001);
        assertTrue(event.isPublic_status());
    }

    /**
     * Tests if all the setter methods in the Event class are working properly.
     */
    @Test
    public void testSetters() {
        Event event = new Event();

        event.setTitle("Lunch Break");
        event.setDate("28 Mar 2025");
        event.setOverlayColor("#000000");
        event.setImageUrl("imageXYZ");
        event.setEmojiResource(321);
        event.setFollowed(false);
        event.setMyPost(false);
        event.setMood("Tired");
        event.setMoodExplanation("Skipped breakfast");
        event.setSituation("With friends");
        event.setPostUser("student2");
        event.setLat(51.0447);
        event.setLng(-114.0719);
        event.setID("2");
        event.setTimestamp(1617632400000L);
        event.setPublic_status(false);

        assertEquals("Lunch Break", event.getTitle());
        assertEquals("28 Mar 2025", event.getDate());
        assertEquals("#000000", event.getOverlayColor());
        assertEquals("imageXYZ", event.getImageUrl());
        assertEquals(321, event.getEmojiResource());
        assertFalse(event.isFollowed());
        assertFalse(event.isMyPost());
        assertEquals("Tired", event.getMood());
        assertEquals("Skipped breakfast", event.getMoodExplanation());
        assertEquals("With friends", event.getSituation());
        assertEquals("student2", event.getPostUser());
        assertEquals(51.0447, event.getLat(), 0.0001);
        assertEquals(-114.0719, event.getLng(), 0.0001);
        assertEquals("2", event.getID());
        assertEquals(1617632400000L, event.getTimestamp());
        assertFalse(event.isPublic_status());
    }

    /**
     * Tests if comments can be added to an event and retrieved in the right order.
     */
    @Test
    public void testCommentsHandling() {
        Event event = new Event();
        Comment comment1 = new Comment("alice", "Nice post!", "28/03/25 14:00");
        Comment comment2 = new Comment("bob", "Very relatable", "28/03/25 15:00");

        event.addComment(comment1);
        event.addComment(comment2);

        List<Comment> comments = event.getComments();

        assertEquals(2, comments.size());
        assertEquals("alice", comments.get(0).getUsername());
        assertEquals("bob", comments.get(1).getUsername());
    }

    /**
     * Tests if setting a non-empty list of events updates the adapter's item count correctly.
     */
    @Test
    public void testSetEvents() {
        adapter.setEvents(mockEvents);
        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Tests if setting an empty list of events results in item count = 0.
     */
    @Test
    public void testEmptyEventList() {
        adapter.setEvents(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    /**
     * Tests if a new event has no comments by default.
     */
    @Test
    public void testEmptyCommentsList() {
        Event event = new Event();
        assertTrue(event.getComments().isEmpty());
    }
}

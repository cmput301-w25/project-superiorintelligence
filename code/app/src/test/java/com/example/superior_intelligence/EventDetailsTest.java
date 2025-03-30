package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

public class EventDetailsTest {

    private Event mockEvent;
    private Database mockDb;
    private EventManager eventManager;

    private Comment mockComment() {
        return new Comment("testuser", "Nice post!", "30/03/25 12:00");
    }

    @Before
    public void setUp() {
        mockEvent = new Event();
        mockEvent.setID("123");
        mockEvent.setPublic_status(false);
        mockDb = Mockito.mock(Database.class);
        eventManager = new EventManager();
    }

    @Test
    public void testParseComments_SingleUser() {
        Map<String, List<Map<String, String>>> commentsMap = new HashMap<>();
        Map<String, String> commentData = new HashMap<>();
        commentData.put("text", "Nice post!");
        commentData.put("date", "30/03/25 12:00");

        List<Map<String, String>> userComments = new ArrayList<>();
        userComments.add(commentData);

        commentsMap.put("testuser", userComments);

        List<Comment> result = EventManager.parseComments(commentsMap);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("Nice post!", result.get(0).getText());
        assertEquals("30/03/25 12:00", result.get(0).getTime());
    }

    @Test
    public void testParseComments_MultipleUsers() {
        Map<String, List<Map<String, String>>> commentsMap = new HashMap<>();

        commentsMap.put("user1", Arrays.asList(
                Map.of("text", "Comment A", "date", "01/01/25 10:00")
        ));
        commentsMap.put("user2", Arrays.asList(
                Map.of("text", "Comment B", "date", "01/01/25 10:05"),
                Map.of("text", "Comment C", "date", "01/01/25 10:10")
        ));

        List<Comment> result = EventManager.parseComments(commentsMap);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getUsername().equals("user2") && c.getText().equals("Comment C")));
    }

    @Test
    public void testParseComments_EmptyMap() {
        List<Comment> result = EventManager.parseComments(new HashMap<>());
        assertEquals(0, result.size());
    }

    @Test
    public void testParseComments_NullMap() {
        List<Comment> result = EventManager.parseComments(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testDeleteEvent_Success() {
        Mockito.doAnswer(invocation -> {
            Database.OnEventDeletedCallback cb = invocation.getArgument(1);
            cb.onEventDeleted(true);
            return null;
        }).when(mockDb).deleteEvent(Mockito.eq("123"), Mockito.any());

        eventManager.deleteEvent(mockDb, "123", success -> assertTrue(success));
    }

    @Test
    public void testDeleteEvent_Failure() {
        Mockito.doAnswer(invocation -> {
            Database.OnEventDeletedCallback cb = invocation.getArgument(1);
            cb.onEventDeleted(false);
            return null;
        }).when(mockDb).deleteEvent(Mockito.eq("123"), Mockito.any());

        eventManager.deleteEvent(mockDb, "123", success -> assertFalse(success));
    }


    @Test
    public void testUpdateStatus_ChangeStatus_Success() {
        mockEvent.setPublic_status(false);

        Mockito.doAnswer(invocation -> {
            Database.OnEventUpdateListener cb = invocation.getArgument(1);
            cb.onEventUpdated(true);
            return null;
        }).when(mockDb).updateEvent(Mockito.eq(mockEvent), Mockito.any());

        eventManager.updateEventStatus(mockDb, mockEvent, true, success -> assertTrue(success));
    }

    @Test
    public void testUpdateStatus_ChangeStatus_Failure() {
        mockEvent.setPublic_status(false);

        Mockito.doAnswer(invocation -> {
            Database.OnEventUpdateListener cb = invocation.getArgument(1);
            cb.onEventUpdated(false);
            return null;
        }).when(mockDb).updateEvent(Mockito.eq(mockEvent), Mockito.any());

        eventManager.updateEventStatus(mockDb, mockEvent, true, success -> assertFalse(success));
    }

}



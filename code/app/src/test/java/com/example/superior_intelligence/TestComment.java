package com.example.superior_intelligence;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestComment {

    private List<Comment> createMockList() {
        List<Comment> list = new ArrayList<>();
        list.add(new Comment("ken", "This is great!", "29/03/25 15:00"));
        return list;
    }

    @Test
    public void testAddComment() {
        List<Comment> list = createMockList();
        assertEquals(1, list.size());

        Comment newComment = new Comment("alex", "I agree", "29/03/25 15:02");
        list.add(newComment);

        assertEquals(2, list.size());
        assertTrue(list.contains(newComment));
    }

    @Test
    public void testPreventDuplicateManually() {
        List<Comment> list = createMockList();
        Comment duplicate = new Comment("ken", "This is great!", "29/03/25 15:00");

        assertTrue(list.contains(duplicate));
    }

    @Test
    public void testCommentEqual() {
        Comment c1 = new Comment("ken", "Hi", "29/03/25 12:00");
        Comment c2 = new Comment("ken", "Hi", "29/03/25 12:00");

        assertEquals(c1, c2);
    }

    @Test
    public void testSortByDate() {
        List<Comment> list = new ArrayList<>();
        list.add(new Comment("zoe", "Third", "30/03/25 18:00"));
        list.add(new Comment("ken", "Second", "29/03/25 15:00"));
        list.add(new Comment("alice", "First", "28/03/25 12:00"));

        Collections.sort(list);

        assertEquals("First", list.get(0).getText());
        assertEquals("Second", list.get(1).getText());
        assertEquals("Third", list.get(2).getText());
    }
}

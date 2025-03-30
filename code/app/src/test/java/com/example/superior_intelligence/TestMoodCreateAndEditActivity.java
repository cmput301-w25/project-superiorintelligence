package com.example.superior_intelligence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for helper methods in MoodCreateAndEditActivity.
 */
public class TestMoodCreateAndEditActivity {

    private MoodCreateAndEditActivity activity;

    @Before
    public void setUp() {
        activity = Mockito.spy(new MoodCreateAndEditActivity()); // Spy to track method calls
    }

    /**
     * Tests the creation of a new Event object.
     */
    @Test
    public void testCreateNewEvent() {
        Event event = activity.createNewEvent();

        assertNotNull("Event should not be null", event);
        assertEquals("Default overlay color should be gold", "#FFD700", event.getOverlayColor());
        assertEquals("Default image URL should be empty", "", event.getImageUrl());
        assertTrue("Event should be marked as 'my post'", event.isMyPost());
        assertNotNull("Event date should not be null", event.getDate());
    }

    /**
     * Tests if updateEmojiIcon() returns the correct resource ID for each mood.
     */
    @Test
    public void testUpdateEmojiIcon() {
        assertEquals(R.drawable.angry_icon, activity.updateEmojiIcon("anger"));
        assertEquals(R.drawable.happy_icon, activity.updateEmojiIcon("happiness"));
        assertEquals(R.drawable.sad_icon, activity.updateEmojiIcon("sadness"));
        assertEquals(R.drawable.disgust, activity.updateEmojiIcon("disgust"));
        assertEquals(R.drawable.confusion, activity.updateEmojiIcon("confusion"));
        assertEquals(R.drawable.fear, activity.updateEmojiIcon("fear"));
        assertEquals(R.drawable.shame, activity.updateEmojiIcon("shame"));
        assertEquals(R.drawable.surprise, activity.updateEmojiIcon("surprise"));
        assertEquals(R.drawable.happy_icon, activity.updateEmojiIcon("random_mood")); // Default case
    }

    /**
     * Tests handleConfirmClick() ensures emotion is selected before confirming.
     */
    @Test
    public void testHandleConfirmClick() {
        // Case 1: No emotion selected → should show a toast
        activity.isEmotionSelected = false;

        // Mock Toast to prevent real UI interaction
        doNothing().when(activity).runOnUiThread(any());

        activity.handleConfirmClick();

        verify(activity).runOnUiThread(any()); // Ensure UI action (Toast) was triggered

        // Case 2: Emotion selected → should create event and start HomeActivity
        activity.isEmotionSelected = true;
        doNothing().when(activity).startActivity(any(Intent.class)); // Prevent real activity launch

        activity.handleConfirmClick();

        verify(activity).startActivity(any(Intent.class)); // Ensure activity transition occurs
    }
}
package com.example.superior_intelligence;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TestMoodCreateAndEditActivity {

    private ActivityScenario<MoodCreateAndEditActivity> scenario;

    @Before
    public void setUp() {
        // Launch the activity properly using ActivityScenario
        scenario = ActivityScenario.launch(MoodCreateAndEditActivity.class);
    }

    @Test
    public void testUpdateEmojiIcon() {
        scenario.onActivity(activity -> {
            int emojiRes = activity.updateEmojiIcon("Happiness");
            assertEquals(R.drawable.happy_icon, emojiRes);
        });
    }

    @Test
    public void testHandleConfirmClick() {
        scenario.onActivity(activity -> {
            activity.handleConfirmClick(); // Ensure no crash occurs
            assertNotNull(activity); // Just a basic check
        });
    }

    @Test
    public void testCreateNewEvent() {
        scenario.onActivity(activity -> {
            // Mock user inputs
            activity.headerTitle.setText("Test Mood Event");
            activity.selectedMood.setText("Happiness");
            activity.triggerExplanation.setText("Had a great day!");
            activity.selectedSituation.setText("Alone");

            // Set other necessary states
            activity.isEmotionSelected = true; // Simulate an emotion being chosen

            // Call the function under test
            Event newEvent = activity.createNewEvent();

            // Assertions to check if the event contains correct data
            assertNotNull(newEvent);
            assertEquals("Test Mood Event", newEvent.getTitle());
            assertEquals("Happiness", newEvent.getMood());
            assertEquals("Had a great day!", newEvent.getMoodExplanation());
            assertEquals("Alone", newEvent.getSituation());
            assertNotNull(newEvent.getDate()); // Should be auto-generated
        });
    }

}
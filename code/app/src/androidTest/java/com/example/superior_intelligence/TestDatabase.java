package com.example.superior_intelligence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for the Database class using the Firebase Firestore emulator.
 * These tests verify basic CRUD operations such as saving, updating, deleting,
 * and retrieving events, as well as adding comments.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private Database db;
    private FirebaseFirestore firestore;

    /**
     * Initializes Firebase and connects to the Firestore emulator before each test.
     * Prevents IllegalStateException if emulator was already initialized.
     */
    @Before
    public void setup() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        try {
            firestore.useEmulator("10.0.2.2", 8080);
        } catch (IllegalStateException ignored) {
            // Already connected to emulator — safe to ignore
        }

        db = Database.getInstance();
    }

    /**
     * Tests that an Event is successfully saved to Firestore via the emulator.
     */
    @Test
    public void testSaveEventToFirebase_savesSuccessfully() throws InterruptedException {
        Event event = new Event();
        event.setID("test-event-id");
        event.setTitle("Test Event");
        event.setPostUser("testuser");
        event.setPublic_status(true);

        CountDownLatch latch = new CountDownLatch(1);

        db.saveEventToFirebase(event, success -> {
            assertTrue(success);
            latch.countDown();
        });

        assertTrue("Save event timed out", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that an existing Event is successfully updated in Firestore.
     */
    @Test
    public void testUpdateEvent_updatesSuccessfully() throws InterruptedException {
        Event event = new Event();
        event.setID("test-event-id");
        event.setTitle("Updated Title");
        event.setPostUser("testuser");

        CountDownLatch latch = new CountDownLatch(1);

        db.updateEvent(event, success -> {
            assertTrue(success);
            latch.countDown();
        });

        assertTrue("Update event timed out", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that an Event is deleted from Firestore using its ID.
     */
    @Test
    public void testDeleteEvent_removesEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        db.deleteEvent("test-event-id", success -> {
            assertTrue(success);
            latch.countDown();
        });

        assertTrue("Delete event timed out", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that a Comment is added to a specific Event document in Firestore.
     * Waits briefly to allow Firestore to process the nested comment addition.
     */
    @Test
    public void testSaveCommentToEvent_addsComment() throws InterruptedException {
        Comment comment = new Comment();
        comment.setUsername("testuser");
        comment.setText("This is a comment.");
        comment.setTime("12:00 PM");

        db.saveCommentToEvent("test-event-id", comment);

        SystemClock.sleep(2000); // Wait for Firestore to process
        // Optional: verify structure if needed
    }

    /**
     * Tests loading and categorizing events from Firestore based on user role.
     * The result is split into my posts, explore, and followed lists.
     */
    @Test
    public void testLoadEventsFromFirebase_loadsAndCategorizes() throws InterruptedException {
        User testUser = mock(User.class);
        when(testUser.getUsername()).thenReturn("testuser");

        CountDownLatch latch = new CountDownLatch(1);

        db.loadEventsFromFirebase(testUser, (myPosts, explore, followed) -> {
            assertNotNull(myPosts);
            assertNotNull(explore);
            assertNotNull(followed);
            latch.countDown();
        });

        assertTrue("Load events timed out", latch.await(5, TimeUnit.SECONDS));
    }
}

package com.example.superior_intelligence;

import static org.junit.Assert.*;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for the Userbase class using the Firebase Firestore emulator.
 * These tests verify user existence checking functionality.
 */
@RunWith(AndroidJUnit4.class)
public class UserbaseTest {

    private Userbase userbase;
    private FirebaseFirestore firestore;

    /**
     * Initializes Firebase and connects to the Firestore emulator before each test.
     * Prevents IllegalStateException if emulator was already initialized.
     */
    @Before
    public void setup() {
        // Initialize Firebase with the application context
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        // Get Firestore instance and configure it to use the emulator
        firestore = FirebaseFirestore.getInstance();
        try {
            firestore.useEmulator("10.0.2.2", 8080); // Default emulator host/port for Android
        } catch (IllegalStateException ignored) {
            // Already connected to emulator — safe to ignore
        }

        // Instantiate Userbase (uses the default constructor with real Firestore)
        userbase = Userbase.getInstance();

        // Optional: Clear Firestore data between tests to ensure a clean state
        firestore.clearPersistence();
    }

    /**
     * Tests that checkUserExists returns true with correct details when a user exists in Firestore.
     */
    @Test
    public void testCheckUserExists_UserExists() throws InterruptedException {
        // Arrange: Add a test user to Firestore emulator
        CountDownLatch setupLatch = new CountDownLatch(1);
        firestore.collection("users").document("testUser")
                .set(new HelperClass("Test Name", "testUser", "password123"))
                .addOnSuccessListener(aVoid -> setupLatch.countDown())
                .addOnFailureListener(e -> fail("Failed to setup test user: " + e.getMessage()));

        assertTrue("Setup timed out", setupLatch.await(5, TimeUnit.SECONDS));

        // Act & Assert: Check if the user exists
        CountDownLatch latch = new CountDownLatch(1);
        userbase.checkUserExists("testUser", (exists, name, username, password) -> {
            assertTrue(exists);
            assertEquals("Test Name", name);
            assertEquals("testUser", username);
            assertEquals("password123", password);
            latch.countDown();
        });

        assertTrue("Check user exists timed out", latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that checkUserExists returns false with null values when a user does not exist.
     */
    @Test
    public void testCheckUserExists_UserDoesNotExist() throws InterruptedException {
        // Act & Assert: Check a non-existent user
        CountDownLatch latch = new CountDownLatch(1);
        userbase.checkUserExists("nonExistentUser", (exists, name, username, password) -> {
            assertFalse(exists);
            assertNull(name);
            assertNull(username);
            assertNull(password);
            latch.countDown();
        });

        assertTrue("Check user does not exist timed out", latch.await(5, TimeUnit.SECONDS));
    }
}
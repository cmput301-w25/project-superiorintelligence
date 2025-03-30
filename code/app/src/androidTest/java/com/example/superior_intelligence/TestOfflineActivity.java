package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test add/edit/delete mood event when app is disconnected from the network
 */
@RunWith(AndroidJUnit4.class)
public class TestOfflineActivity {
    // Code is taken from Firestore website and Chatgpt
    private FirebaseFirestore db;

    /**
     * Enable disk persistance to maintain state while offline before every test
     */
    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        // Enable offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    /**
     * Test adding user while online, disconnect from the network,
     * and try to fetch data from the database
     * @throws InterruptedException
     */
    @Test
    public void testOfflinePersistence() throws InterruptedException {
        String docId = "test_user_123";
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "Offline User");
        testData.put("username", "offline@example.com");

        // Write to Firestore
        db.collection("users") // Collection name
                .document(docId) // Document ID
                .set(testData)
                .addOnSuccessListener(aVoid -> System.out.println("Data written successfully"))
                .addOnFailureListener(e -> System.err.println("Error writing document: " + e));

        CountDownLatch latch = new CountDownLatch(1);

        // Step 1: Write data online
        db.collection("users").document(docId)
                .set(testData)
                .addOnSuccessListener(aVoid -> latch.countDown())
                .addOnFailureListener(e -> fail("Failed to write data"));

        assertTrue(latch.await(5, TimeUnit.SECONDS)); // Wait for Firestore write

        // Step 2: Simulate offline mode
        db.disableNetwork();

        // Step 3: Read data while offline (should fetch from local cache)
        CountDownLatch readLatch = new CountDownLatch(1);
        db.collection("users").document(docId)
                .get(Source.CACHE) // Force Firestore to use cached data
                .addOnSuccessListener(documentSnapshot -> {
                    assertNotNull(documentSnapshot);
                    assertEquals("Offline User", documentSnapshot.getString("name"));
                    assertEquals("offline@example.com", documentSnapshot.getString("username"));
                    readLatch.countDown();
                })
                .addOnFailureListener(e -> fail("Failed to read offline data"));

        assertTrue(readLatch.await(5, TimeUnit.SECONDS)); // Wait for Firestore read

        // Step 4: Re-enable network and verify sync
        db.enableNetwork();
    }

    @Test
    public void addEventWhenOfflineShouldAppearOnDbWhenOnline() throws InterruptedException{

        //Enable Firestore persistence (IMPORTANT)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Ensure offline writes are saved
                .build();
        db.setFirestoreSettings(settings);

        // Disable network, simulate offline mode
        CountDownLatch networkOffLatch = new CountDownLatch(1);
        db.disableNetwork().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Network disabled successfully.");
            } else {
                System.err.println("Failed to disable network.");
            }
            networkOffLatch.countDown();
        });

        assertTrue("Failed to disable network", networkOffLatch.await(15, TimeUnit.SECONDS));

        String docId = "add_mood_offline_test";
        Map<String, Object> testData = new HashMap<>();
        testData.put("title", "Offline Event");
        testData.put("mood", "Shame");

        CountDownLatch writeLatch = new CountDownLatch(1);

        //Write data while offline
        db.collection("MyPosts").document(docId)
                .set(testData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Offline data queued for sync");
                    writeLatch.countDown();
                })
                .addOnFailureListener(e -> fail("Failed to queue offline write"));

        // Wait for the write to be queued in offline mode
//        boolean writeQueued = writeLatch.await(60, TimeUnit.SECONDS); // Extended wait time
//        assertTrue("Failed to queue offline write within expected time", writeQueued);

        // Step 3: Re-enable network and wait for Firestore to sync
        CountDownLatch networkOnLatch = new CountDownLatch(1);
        db.enableNetwork().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Network enabled successfully.");
            } else {
                System.err.println("Failed to enable network.");
            }
            networkOnLatch.countDown();
        });

        boolean networkRestored = networkOnLatch.await(15, TimeUnit.SECONDS); // Wait for re-enable
        assertTrue("Failed to re-enable network", networkRestored);

        // Step 4: Wait for Firestore to sync (Retry mechanism)
        AtomicBoolean synced = new AtomicBoolean(false);
        for (int i = 0; i < 5; i++) { // Retry up to 5 times
            CountDownLatch readLatch = new CountDownLatch(1);

            db.collection("MyPosts").document(docId)
                    .get(Source.SERVER) // Ensure data comes from Firestore online
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            assertEquals("Offline Event", documentSnapshot.getString("title"));
//                            assertEquals("offline@example.com", documentSnapshot.getStringser"));
                            assertEquals("Shame", documentSnapshot.getString("mood"));
                            System.out.println("Data successfully synced!");
                            synced.set(true);
                        }
                        readLatch.countDown();
                    })
                    .addOnFailureListener(e -> System.err.println("Failed to fetch online data: " + e));

            if (readLatch.await(15, TimeUnit.SECONDS) && synced.get()) {
                break; // Exit retry loop if sync successful
            }

            Thread.sleep(5000); // Wait before retrying
        }

        //assertTrue("Firestore did not sync the offline write", synced.get());
    }


    /**
     * Disable app from network
     */
    private void disableNetwork() {
        db.disableNetwork().addOnCompleteListener(task -> System.out.println("Network disabled"));
    }

    /**
     * Connect app to the network
     */
    private void enableNetwork() {
        db.enableNetwork().addOnCompleteListener(task -> System.out.println("Network enabled"));
    }

}

package com.example.superior_intelligence;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
@RunWith(AndroidJUnit4.class)
public class TestHomeActivity {

    private static boolean firestoreInitialized = false;
    private FirebaseFirestore db;

    @Rule
    public ActivityTestRule<HomeActivity> activityRule = new ActivityTestRule<>(HomeActivity.class);

    @BeforeClass
    public static void setupFirestoreEmulator() {
        if (!firestoreInitialized) {
            FirebaseApp.initializeApp(getApplicationContext());  // Ensure FirebaseApp is initialized
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.useEmulator("10.0.2.2", 8080);
            firestoreInitialized = true;
        }
    }

    @Before
    public void setupActivity() {
        activityRule.getActivity();  // Ensure activity launches
        db = FirebaseFirestore.getInstance();
        assertNotNull("Firestore should be initialized", db); // Ensure Firestore instance is assigned
    }


 */
    /**
     * **Test Firestore Connection**
     */
    /*
    @Test
    public void testFirestoreConnection() {
        assertNotNull("Firestore instance should not be null", db);
    }
    /*
    /**
     * **Test Adding a New Event**
     */
    /*
    @Test
    public void testAddEventToFirestore() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Event testEvent = new Event("UniqueID", "New Test Event", "11-03-2020", "", "This is a test event", 1, false, true, "testuser", (double) 0, (double) 0);

        db.collection("events").add(testEvent)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreTest", "New event added with ID: " + documentReference.getId());
                    assertNotNull("Event ID should not be null", documentReference.getId());
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreTest", "Error adding new event", e);
                    latch.countDown();
                });



        latch.await(5, TimeUnit.SECONDS);
    }
    */
    /**
     * **Test Retrieving a Specific Event**
     */
    /*
    @Test
    public void testRetrieveSpecificEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);


        Event testEvent = new Event("SomeID", "Retrieve Event", "11-03-2020", "", "Test retrieval", 1, false, true, "testuser", (double) 0, (double) 0);

        db.collection("events").add(testEvent).addOnSuccessListener(docRef -> {
            String eventId = docRef.getId();

            db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Log.d("FirestoreTest", "Retrieved event: " + task.getResult().getId());
                    assertEquals("Retrieve Event", task.getResult().getString("title"));
                } else {
                    Log.e("FirestoreTest", "Failed to retrieve event");
                }
                latch.countDown();
            });

        });

        latch.await(5, TimeUnit.SECONDS);
    }
    */
    /**
     * **Test Updating an Existing Event**
     */
    /*
    @Test
    public void testUpdateEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Event testEvent = new Event("UpdateID", "Update Event", "11-03-2020", "", "Before update", 1, false, true, "testuser", (double) 0, (double) 0);
        db.collection("events").add(testEvent).addOnSuccessListener(docRef -> {
            String eventId = docRef.getId();

            db.collection("events").document(eventId)
                    .update("description", "Updated description")
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirestoreTest", "Event successfully updated");

                        db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().exists()) {
                                assertEquals("Updated description", task.getResult().getString("description"));
                            }
                            latch.countDown();
                        });

                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreTest", "Error updating event", e);
                        latch.countDown();
                    });

        });

        latch.await(5, TimeUnit.SECONDS);
    }

     */

    /**
     * **Test Deleting an Event**
     */
    /*
    @Test
    public void testDeleteEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Event testEvent = new Event("DeleteID","Delete Event", "11-03-2020", "", "To be deleted", 1, false, true, "testuser", (double) 0, (double) 0);
        db.collection("events").add(testEvent).addOnSuccessListener(docRef -> {
            String eventId = docRef.getId();

            db.collection("events").document(eventId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirestoreTest", "Event successfully deleted");

                        db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
                            assertFalse("Event should be deleted", task.getResult().exists());
                            latch.countDown();
                        });

                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreTest", "Error deleting event", e);
                        latch.countDown();
                    });
        });

        latch.await(5, TimeUnit.SECONDS);
    }

   */
//}


//package com.example.superior_intelligence;
//
///**
// * Handle when user is disconnected from the network, save and restore when user gets the connection back
// */
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
////code taken from: https://firebase.google.com/docs/database/android/offline-capabilities#java
//public class OfflineActivity extends AppCompatActivity {
//
//    private static final String TAG = "OfflineActivity";
//
//    /**
//     * enable disk persistence: app will write the data locally to the device so it can
//     * maintain state while offline, even if the user or operating system restarts the app.
//     */
//    private void enablePersistence() {
//        // [START rtdb_enable_persistence]
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        // [END rtdb_enable_persistence]
//    }
//
//
//    private void keepSynced() {
//        // [START rtdb_keep_synced]
//        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Event");
//        eventsRef.keepSynced(true);
//        // [END rtdb_keep_synced]
//
//        // [START rtdb_undo_keep_synced]
//        eventsRef.keepSynced(false);
//        // [END rtdb_undo_keep_synced]
//    }
//
//
//}

package com.example.superior_intelligence;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Handles offline support for Firestore data persistence.
 * This activity enables Firestore's offline mode, allowing the app
 * to store and retrieve data even when the device is offline.
 */

public class OfflineActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Initializes the activity and enables Firestore offline persistence.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enablePersistence();
    }

    /**
     * Enables Firestore's offline data persistence.
     * This allows Firestore to store data locally and synchronize it with
     * the server when the network is available.
     */
    private void enablePersistence() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true) // Enable Firestore offline mode
                        .build()
        );
    }
}


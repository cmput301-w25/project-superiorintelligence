package com.example.superior_intelligence;

/**
 * Handle when user is disconnected from the network, save and restore when user gets the connection back
 */

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//code taken from: https://firebase.google.com/docs/database/android/offline-capabilities#java
public class OfflineActivity extends AppCompatActivity {

    private static final String TAG = "OfflineActivity";

    /**
     * enable disk persistence: app will write the data locally to the device so it can
     * maintain state while offline, even if the user or operating system restarts the app.
     */
    private void enablePersistence() {
        // [START rtdb_enable_persistence]
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // [END rtdb_enable_persistence]
    }


    private void keepSynced() {
        // [START rtdb_keep_synced]
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Event");
        eventsRef.keepSynced(true);
        // [END rtdb_keep_synced]

        // [START rtdb_undo_keep_synced]
        eventsRef.keepSynced(false);
        // [END rtdb_undo_keep_synced]
    }


}

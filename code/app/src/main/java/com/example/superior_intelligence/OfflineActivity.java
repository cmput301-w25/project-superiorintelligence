package com.example.superior_intelligence;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Handles offline support for Firestore data persistence.
 * This activity enables Firestore's offline mode, allowing the app
 * to store and retrieve data even when the device is offline.
 * issue: us 07.01.01, Link to sub issue:
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


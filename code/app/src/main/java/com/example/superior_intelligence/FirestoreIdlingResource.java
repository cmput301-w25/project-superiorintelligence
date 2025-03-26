package com.example.superior_intelligence;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingResource;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreIdlingResource implements IdlingResource {
    private ResourceCallback resourceCallback;
    private boolean isIdle = false;
    private final String collection;
    private ListenerRegistration listenerRegistration;

    public FirestoreIdlingResource(String collection) {
        this.collection = collection;
    }

    @Override
    public String getName() {
        return FirestoreIdlingResource.class.getName() + "[" + collection + "]";
    }

    @Override
    public boolean isIdleNow() {
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    public void waitForFirestoreUpdate() {
        isIdle = false;

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = FirebaseFirestore.getInstance()
                .collection(collection)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                        isIdle = true;

                        if (resourceCallback != null) {
                            resourceCallback.onTransitionToIdle();
                        }
                        // remove listener after one successful event to prevent blocking
                        if (listenerRegistration != null) {
                            listenerRegistration.remove();
                            listenerRegistration = null;
                        }
                    }
                });
    }
}

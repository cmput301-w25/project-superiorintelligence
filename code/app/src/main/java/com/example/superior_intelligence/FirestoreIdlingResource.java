package com.example.superior_intelligence;

import androidx.test.espresso.IdlingResource;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreIdlingResource implements IdlingResource {
    private ResourceCallback resourceCallback;
    private boolean isIdle = false;
    private final String collection;

    public FirestoreIdlingResource(String collection) {
        this.collection = collection;
    }

    @Override
    public String getName() { return FirestoreIdlingResource.class.getName(); }

    @Override
    public boolean isIdleNow() { return isIdle; }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) { resourceCallback = callback; }

    public void waitForFirestoreUpdate() {
        isIdle = false;

        FirebaseFirestore.getInstance().collection(collection)
                .get()
                .addOnCompleteListener(task -> {
                    isIdle = true;
                    if (resourceCallback != null) resourceCallback.onTransitionToIdle();
                });
    }


}

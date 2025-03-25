package com.example.superior_intelligence;

import androidx.test.espresso.IdlingResource;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreIdlingResource implements IdlingResource {
    private ResourceCallback resourceCallback;
    private boolean isIdle = false;

    @Override
    public String getName() { return FirestoreIdlingResource.class.getName(); }

    @Override
    public boolean isIdleNow() { return isIdle; }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) { resourceCallback = callback; }

    public void waitForFirestoreUpdate() {
        FirebaseFirestore.getInstance().collection("myposts")
                .get()
                .addOnCompleteListener(task -> {
                    isIdle = true;
                    if (resourceCallback != null) resourceCallback.onTransitionToIdle();
                });
    }
}

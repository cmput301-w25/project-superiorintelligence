package com.example.superior_intelligence;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Database {
    private final FirebaseFirestore db;
    private final CollectionReference myPostsRef;

    public Database() {
        db = FirebaseFirestore.getInstance();
        myPostsRef = db.collection("MyPosts");
    }

    /**
     * Saves a new Event object to Firestore.
     */
    public void saveEventToFirebase(@NonNull Event event, @NonNull OnEventSavedCallback callback) {
        // Convert Event to a Map
        // (Same as your old saveEventToFirebase method in HomeActivity)
        try {
            // We use a helper method:
            myPostsRef.add(Mapper.eventToMap(event))
                    .addOnSuccessListener(documentReference -> {
                        Log.d("EventRepository", "Event saved: " + documentReference.getId());
                        callback.onEventSaved(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("EventRepository", "Error saving event", e);
                        callback.onEventSaved(false);
                    });
        } catch (Exception e) {
            Log.e("EventRepository", "Failed to convert event", e);
            callback.onEventSaved(false);
        }

    }
    /**
     * Loads all events from Firestore, separating them into explore/followed/myPosts
     * based on the current logged in user.
     */
    public void loadEventsFromFirebase(@NonNull User currentUser, @NonNull OnEventsLoadedCallback callback) {
        myPostsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Event> myPosts = new ArrayList<>();
                        List<Event> explore = new ArrayList<>();
                        List<Event> followed = new ArrayList<>();

                        String loggedInUsername = currentUser.getUsername();

                        // Loop all docs
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert doc to Event
                            Event event = Mapper.docToEvent(document);
                            if (event == null) continue;

                            // Decide which list it belongs to
                            if (loggedInUsername.equals(event.getUser())) {
                                myPosts.add(event);
                            } else if (event.isFollowed()) {
                                followed.add(event);
                            } else {
                                explore.add(event);
                            }
                        }

                        callback.onEventsLoaded(myPosts, explore, followed);
                    } else {
                        callback.onEventsLoaded(null, null, null); // indicate failure
                    }
                });
    }
    /**
     * Simple callback for saving events.
     */
    public interface OnEventSavedCallback {
        void onEventSaved(boolean success);
    }

    /**
     * Callback for loading events and splitting them into lists.
     */
    public interface OnEventsLoadedCallback {
        void onEventsLoaded(List<Event> myPosts, List<Event> explore, List<Event> followed);
    }

}
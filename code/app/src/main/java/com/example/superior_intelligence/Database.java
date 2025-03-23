package com.example.superior_intelligence;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static Database instance;
    private final FirebaseFirestore db;
    private final CollectionReference myPostsRef;


    public Database() {
        db = FirebaseFirestore.getInstance();
        myPostsRef = db.collection("MyPosts");
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Saves a new Event object to Firestore.
     */
    public void saveEventToFirebase(@NonNull Event event, @NonNull OnEventSavedCallback callback) {
        // Ensure event has an ID (UUID)
        if (event.getID() == null || event.getID().isEmpty()) {
            event.setID(java.util.UUID.randomUUID().toString());
        }
        try {
            // Use UUID as the document ID
            myPostsRef.document(event.getID()).set(Mapper.eventToMap(event))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EventRepository", "Event saved with ID: " + event.getID());
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
     * Updates an existing Event in Firestore.
     */
    public void updateEvent(@NonNull Event event, @NonNull OnEventUpdateListener callback) {
        try {
            // Convert Event to a Map using your existing Mapper utility
            Map<String, Object> updatedEventData = Mapper.eventToMap(event);

            // Update the document in Firestore
            myPostsRef.document(event.getID()).set(updatedEventData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EventRepository", "Event updated with ID: " + event.getID());
                        callback.onEventUpdated(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("EventRepository", "Error updating event", e);
                        callback.onEventUpdated(false);
                    });
        } catch (Exception e) {
            Log.e("EventRepository", "Failed to convert event for update", e);
            callback.onEventUpdated(false);
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
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        Log.d("DatabaseDebug", "Total posts found in Firestore: " + documents.size());

                        if (documents.isEmpty()) {
                            callback.onEventsLoaded(myPosts, explore, followed);
                            return;
                        }

                        final int totalDocuments = documents.size();
                        final int[] processedCount = {0};

                        // Loop all docs
                        for (DocumentSnapshot document : documents) {
                            Event event = Mapper.docToEvent(document);
                            if (event == null) {
                                Log.e("DatabaseDebug", "Failed to convert document to Event: " + document.getId());
                                processedCount[0]++;
                                continue;
                            }

                            Log.d("DatabaseDebug", "loggedInUsername: " + loggedInUsername);
                            Log.d("DatabaseDebug", "event.getUser(): " + event.getUser());
                            Log.d("DatabaseDebug", "Event Title: " + event.getTitle() + " | User: " + event.getUser());

                            if (loggedInUsername != null && loggedInUsername.equals(event.getUser())) {
                                myPosts.add(event);
                                processedCount[0]++;
                                if (processedCount[0] == totalDocuments) {
                                    Log.d("DatabaseDebug", "Final Event Counts -> MyPosts: " + myPosts.size() + ", Explore: " + explore.size() + ", Followed: " + followed.size());
                                    callback.onEventsLoaded(myPosts, explore, followed);
                                }
                            } else {
                                Userbase.getInstance().checkFollowStatus(loggedInUsername, event.getUser(), isFollowing -> {
                                    if (isFollowing) {
                                        followed.add(event);
                                    } else if (event.isPublic_status()){
                                        explore.add(event);
                                    }
                                    processedCount[0]++;

                                    // Once all documents are processed, trigger callback
                                    if (processedCount[0] == totalDocuments) {
                                        Log.d("DatabaseDebug", "Final Event Counts -> MyPosts: " + myPosts.size() + ", Explore: " + explore.size() + ", Followed: " + followed.size());
                                        callback.onEventsLoaded(myPosts, explore, followed);
                                    }
                                });
                            }
                        }
                    } else {
                        Log.e("DatabaseDebug", "Failed to fetch events from Firestore", task.getException());
                        callback.onEventsLoaded(null, null, null); // Indicate failure
                    }
                });
    }

    public void saveCommentToEvent(String postId, Comment comment) {
        DocumentReference postRef = myPostsRef.document(postId);

        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> data = documentSnapshot.getData();
                Map<String, List<Map<String, String>>> commentsMap;

                if (data != null && data.containsKey("comments")) {
                    commentsMap = (Map<String, List<Map<String, String>>>) data.get("comments");
                } else {
                    commentsMap = new HashMap<>();
                }

                Map<String, String> commentData = new HashMap<>();
                commentData.put("date", comment.getTime());
                commentData.put("text", comment.getText());

                if (!commentsMap.containsKey(comment.getUsername())) {
                    commentsMap.put(comment.getUsername(), new ArrayList<>());
                }
                commentsMap.get(comment.getUsername()).add(commentData);

                postRef.update("comments", commentsMap)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Comment saved successfully"))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error saving comment", e));
            }
        });
    }


    /**
     * Delete Event from Firestore
     */
    public void deleteEvent(String eventID){
        myPostsRef.document(eventID).delete();

    }

    /**
     * Callback for saving events.
     */
    public interface OnEventSavedCallback {
        void onEventSaved(boolean success);
    }

    /**
     * Callback for updating events.
     */
    public interface OnEventUpdateListener {
        void onEventUpdated(boolean success);
    }

    /**
     * Callback for loading events and splitting them into lists.
     */
    public interface OnEventsLoadedCallback {
        void onEventsLoaded(List<Event> myPosts, List<Event> explore, List<Event> followed);
    }

    public CollectionReference getMyPostsRef() {
        return myPostsRef;
    }
}

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
            //Map<String, Object> eventMap = Mapper.eventToMap(event);

            // Step 2: Debug log before saving to Firestore
            //Log.d("DebugFirestore", "Mapped Event before saving: " + eventMap.toString());

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

                        if (currentUser == null) {
                            Log.e("Database", "Null event encountered!");
                            return; // Exit function early
                        }

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
                            Log.d("DatabaseDebug", "Raw Firestore Document: " + document.getData()); // 🔍 Log raw data
                            Event event = Mapper.docToEvent(document);

                            // Null Check: Ensure event is not null
                            if (event == null) {
                                Log.e("DatabaseDebug", "Failed to convert document to Event: " + document.getId());
                                processedCount[0]++;
                                continue;
                            }

                            // Null Check: Ensure event.getUser() is not null
                            if (event.getPostUser() == null) {
                                Log.e("DatabaseDebug", "Event has null user! Event ID: " + document.getId());
                                processedCount[0]++;
                                continue;
                            }

                            Log.d("DebugFirestore", "Loaded event user: " + event.getPostUser());


                            String dtString = document.getString("date");
                            if (dtString != null) {
                                try {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
                                    java.util.Date parsedDate = sdf.parse(dtString);
                                    if (parsedDate != null) {
                                        event.setTimestamp(parsedDate.getTime());
                                    } else {
                                        event.setTimestamp(0);
                                    }
                                } catch (java.text.ParseException e) {
                                    e.printStackTrace();
                                    event.setTimestamp(0);
                                }
                            } else {
                                event.setTimestamp(0);
                            }

                            Log.d("DatabaseDebug", "loggedInUsername: " + loggedInUsername);
                            Log.d("DatabaseDebug", "event.getUser(): " + event.getPostUser());
                            Log.d("DatabaseDebug", "Event Title: " + event.getTitle() + " | User: " + event.getPostUser());

                            if (loggedInUsername != null && loggedInUsername.equals(event.getPostUser())) {
                                myPosts.add(event);
                                processedCount[0]++;
                                if (processedCount[0] == totalDocuments) {
                                    // Sort all lists by numeric timestamp descending
                                    myPosts.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                                    explore.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                                    followed.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                                    Log.d("DatabaseDebug", "Final Event Counts -> MyPosts: " + myPosts.size() + ", Explore: " + explore.size() + ", Followed: " + followed.size());
                                    callback.onEventsLoaded(myPosts, explore, followed);
                                }
                            } else {
                                Userbase.getInstance().checkFollowStatus(loggedInUsername, event.getPostUser(), isFollowing -> {
                                    // if event is private, don't add to any tab
                                    if (event.isPublic_status()) {
                                        if (isFollowing) {
                                            followed.add(event);
                                        } else {
                                            explore.add(event);
                                        }
                                    }
                                    processedCount[0]++;
                                    // Once all documents are processed, trigger callback
                                    if (processedCount[0] == totalDocuments) {
                                        myPosts.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                                        explore.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                                        followed.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
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
    public void deleteEvent(String eventID, OnEventDeletedCallback callback) {
        myPostsRef.document(eventID).delete()
            .addOnSuccessListener(aVoid -> {
            Log.d("Database", "Event deleted successfully: " + eventID);
            callback.onEventDeleted(true);
        })
                .addOnFailureListener(e -> {
                    Log.e("Database", "Failed to delete event: " + eventID, e);
                    callback.onEventDeleted(false);
                });
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

    public interface OnEventDeletedCallback {
        void onEventDeleted(boolean success);
    }
}

package com.example.superior_intelligence;

import android.util.Log;

import com.example.superior_intelligence.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Userbase {
    private final FirebaseFirestore db;
    private static Userbase instance;
    public Userbase() {
        db = FirebaseFirestore.getInstance();
    }

    public static Userbase getInstance() {
        if (instance == null) {
            instance = new Userbase();
        }
        return instance;
    }

    public void checkUserExists(String username, UserCheckCallback callback) {
        db.collection("users")
                .document(username)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        callback.onUserChecked(true, name, username);
                    } else {
                        callback.onUserChecked(false, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onUserChecked(false, null, null);
                });
    }

    public void getUserDetails(String username, UserDetailsCallback callback) {
        if (username == null || username.isEmpty()) {
            callback.onUserDetailsFetched(false, null, null);
            return;
        }

        db.collection("users")
                .document(username)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        callback.onUserDetailsFetched(true, username, name);
                    } else {
                        callback.onUserDetailsFetched(false, null, null);
                    }
                })
                .addOnFailureListener(e -> callback.onUserDetailsFetched(false, null, null));
    }


    public void createUser(String name, String username, UserCreationCallback callback) {
        // In this example, we use a simple User (or HelperClass) object.
        // You can also rename HelperClass to something like SignUpUser if you prefer.
        HelperClass userData = new HelperClass(name, username);
        db.collection("users")
                .document(username)
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onUserCreated(true))
                .addOnFailureListener(e -> callback.onUserCreated(false));
    }


    public void unfollowUser(String currentUser, String targetUser, FollowActionCallback callback) {
        db.collection("users").document(currentUser)
                .update("following", FieldValue.arrayRemove(targetUser))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUser)
                            .update("followers", FieldValue.arrayRemove(currentUser))
                            .addOnSuccessListener(aVoid2 -> callback.onFollowAction(true)) // Success
                            .addOnFailureListener(e -> callback.onFollowAction(false)); // Failed to remove from "followers"
                })
                .addOnFailureListener(e -> callback.onFollowAction(false)); // Failed to remove from "following"
    }

    public void checkFollowStatus(String currentUser, String targetUser, FollowCheckCallback callback) {
        db.collection("user")
                .document(currentUser)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFollowChecked(false);
                    } else {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        boolean isFollowing = (following != null && following.contains(targetUser));
                        callback.onFollowChecked(isFollowing);
                    }
                })
                .addOnFailureListener(e -> callback.onFollowChecked(false));
    }

    public void checkFollowRequest(String requester, String requested, FollowRequestCheckCallback callback) {
        db.collection("follow_requests")
                .whereEqualTo("requester", requester)
                .whereEqualTo("requested", requested)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onFollowRequestChecked(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> callback.onFollowRequestChecked(false));
    }

    public void sendFollowRequest(String requester, String requested, FollowRequestActionCallback callback) {
        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("requester", requester);
        requestData.put("requested", requested);
        requestData.put("timestamp", System.currentTimeMillis());

        db.collection("follow_requests")
                .add(requestData)
                .addOnSuccessListener(documentReference -> callback.onFollowRequestAction(true))
                .addOnFailureListener(e -> callback.onFollowRequestAction(false));
    }

    public void getIncomingFollowRequests(String username, FollowRequestListCallback callback) {
        db.collection("follow_requests")
                .whereEqualTo("requested", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> requests = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String requester = doc.getString("requester");
                        if (requester != null) {
                            requests.add(requester);
                            Log.d("Userbase", "Incoming Request from: " + requester);
                        }
                    }
                    callback.onFollowRequestsFetched(requests);
                })
                .addOnFailureListener(e -> callback.onFollowRequestsFetched(new ArrayList<>()));
    }

    public void getPendingFollowRequests(String requesterUsername, FollowRequestListCallback callback) {
        db.collection("follow_requests")
                .whereEqualTo("requester", requesterUsername) // Get requests sent by this user
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> requests = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String requested = doc.getString("requested");
                        if (requested != null) {
                            requests.add(requested);
                            Log.d("Userbase", "Pending Request to: " + requested);
                        }
                    }
                    callback.onFollowRequestsFetched(requests);
                })
                .addOnFailureListener(e -> callback.onFollowRequestsFetched(new ArrayList<>()));
    }

    public void removeFollowRequest(String requester, String requested) {
        db.collection("follow_requests")
                .whereEqualTo("requester", requester)
                .whereEqualTo("requested", requested)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        db.collection("follow_requests").document(doc.getId()).delete();
                    }
                });
    }


    public void getUserFollowing(String username, UserListCallback callback) {
        db.collection("users").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        callback.onUserListRetrieved(following != null ? following : new ArrayList<>());
                    } else {
                        callback.onUserListRetrieved(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onUserListRetrieved(new ArrayList<>()));
    }

    public void acceptFollowRequest(String requester, String requested, FollowActionCallback callback) {
        // Add requester to requested user's "followers" array
        db.collection("users").document(requested)
                .update("followers", FieldValue.arrayUnion(requester))
                .addOnSuccessListener(aVoid -> {
                    // Add requested user to requester's "following" array
                    db.collection("users").document(requester)
                            .update("following", FieldValue.arrayUnion(requested))
                            .addOnSuccessListener(aVoid2 -> {
                                // Remove the follow request
                                db.collection("follow_requests")
                                        .whereEqualTo("requester", requester)
                                        .whereEqualTo("requested", requested)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                                db.collection("follow_requests").document(doc.getId()).delete();
                                            }
                                            callback.onFollowAction(true);
                                        })
                                        .addOnFailureListener(e -> callback.onFollowAction(false));
                            })
                            .addOnFailureListener(e -> callback.onFollowAction(false));
                })
                .addOnFailureListener(e -> callback.onFollowAction(false));
    }

    public void addNotification(String recipientUsername, String message) {
        HashMap<String, Object> notificationData = new HashMap<>();
        notificationData.put("recipient", recipientUsername);
        notificationData.put("message", message);
        notificationData.put("timestamp", System.currentTimeMillis());

        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d("Userbase", "Notification added"))
                .addOnFailureListener(e -> Log.e("Userbase", "Failed to add notification", e));
    }



    /**
     * Callback interface for checking user status.
     */
    public interface UserCheckCallback {
        void onUserChecked(boolean exists, String name, String username);
    }

    /**
     * Callback interface for fetching user details.
     */
    public interface UserDetailsCallback {
        void onUserDetailsFetched(boolean exists, String username, String name);
    }

    /**
     * Callback interface for creating a user.
     */
    public interface UserCreationCallback {
        void onUserCreated(boolean success);
    }

    /**
     * Callback interface for checking following status.
     */
    public interface FollowCheckCallback {
        void onFollowChecked(boolean isFollowing);
    }

    /**
     * Callback interface for following another user.
     */
    public interface FollowActionCallback {
        void onFollowAction(boolean success);
    }

    /**
     * Callback interface for checking if a follow request exists.
     */
    public interface FollowRequestCheckCallback {
        void onFollowRequestChecked(boolean requestExists);
    }

    /**
     * Callback interface for handling follow request actions.
     */
    public interface FollowRequestActionCallback {
        void onFollowRequestAction(boolean success);
    }

    public interface FollowRequestListCallback {
        void onFollowRequestsFetched(List<String> requests);
    }

    public interface UserListCallback {
        void onUserListRetrieved(List<String> users);
    }
}

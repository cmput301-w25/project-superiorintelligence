package com.example.superior_intelligence;

import com.example.superior_intelligence.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

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
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
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
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
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
                .add(userData)
                .addOnSuccessListener(documentReference -> callback.onUserCreated(true))
                .addOnFailureListener(e -> callback.onUserCreated(false));
    }

    public void followUser(String currentUser, String targetUser, FollowActionCallback callback) {
        db.collection("users").document(currentUser)
                .collection("following")
                .document(targetUser)
                .set(new HashMap<>()) // Just storing a reference, no additional data needed
                .addOnSuccessListener(aVoid -> callback.onFollowAction(true))
                .addOnFailureListener(e -> callback.onFollowAction(false));
    }

    public void unfollowUser(String currentUser, String targetUser, FollowActionCallback callback) {
        db.collection("users").document(currentUser)
                .collection("following")
                .document(targetUser)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onFollowAction(true))
                .addOnFailureListener(e -> callback.onFollowAction(false));
    }

    public void checkFollowStatus(String currentUser, String targetUser, FollowCheckCallback callback) {
        db.collection("follows")
                .document(currentUser)
                .collection("following")
                .document(targetUser)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onFollowChecked(true);
                    } else {
                        callback.onFollowChecked(false);
                    }
                })
                .addOnFailureListener(e -> callback.onFollowChecked(false));
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
}

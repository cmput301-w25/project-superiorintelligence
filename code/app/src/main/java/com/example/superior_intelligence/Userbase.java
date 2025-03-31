package com.example.superior_intelligence;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Userbase class that handles user-related operations, such as user creation,
 * checking if a user exists, following and unfollowing users, managing follow requests,
 * and sending notifications. This class interacts with Firestore to store and retrieve user data.
 */
public class Userbase {
    private final FirebaseFirestore db;
    private static Userbase instance;

    /**
     * Initializes the Userbase instance with Firestore.
     */
    public Userbase() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Gets the singleton instance of the Userbase.
     *
     * @return The singleton instance of Userbase.
     */
    public static Userbase getInstance() {
        if (instance == null) {
            instance = new Userbase();
        }
        return instance;
    }

    /**
     * Checks if a user exists in Firestore.
     *
     * @param username The username to check.
     * @param callback The callback to handle the result.
     */
    public void checkUserExists(String username, UserCheckCallback callback) {
        db.collection("users")
                .document(username)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String password = document.getString("password"); // new field
                        callback.onUserChecked(true, name, username, password);
                    } else {
                        callback.onUserChecked(false, null, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onUserChecked(false, null, null, null);
                });
    }

    /**
     * Retrieves user details from Firestore.
     *
     * @param username The username to fetch details for.
     * @param callback The callback to handle the result.
     */
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


    /**
     * Creates a new user in Firestore with the provided details.
     *
     * @param name     The name of the user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param callback The callback to handle the result.
     */
    public void createUser(String name, String username, String password, UserCreationCallback callback) {
        // Use the HelperClass constructor that includes password.
        UserHelper userData = new UserHelper(name, username, password);
        db.collection("users")
                .document(username)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserbaseDebug", "User created: " + username);
                    db.collection("users").document(username).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                List<String> following = (List<String>) documentSnapshot.get("following");
                                if (following != null && following.contains(username)) {
                                    Log.e("UserbaseDebug", "ERROR: User is following itself right after creation!");
                                } else {
                                    Log.d("UserbaseDebug", "Following list is empty after creation, as expected.");
                                }
                            });
                    callback.onUserCreated(true);
                })
                .addOnFailureListener(e -> callback.onUserCreated(false));
    }

    /**
     * Unfollows a user.
     *
     * @param currentUser The user performing the unfollow action.
     * @param targetUser  The user to unfollow.
     * @param callback    The callback to handle the result.
     */
    public void unfollowUser(String currentUser, String targetUser, FollowActionCallback callback) {
        db.collection("users").document(currentUser)
                .update("following", FieldValue.arrayRemove(targetUser))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUser)
                            .update("followers", FieldValue.arrayRemove(currentUser))
                            .addOnSuccessListener(aVoid2 -> callback.onFollowAction(true))
                            .addOnFailureListener(e -> callback.onFollowAction(false));
                })
                .addOnFailureListener(e -> callback.onFollowAction(false));
    }

    /**
     * Checks if one user is following another.
     *
     * @param currentUser The user performing the check.
     * @param targetUser  The user to check if they are followed.
     * @param callback    The callback to handle the result.
     */
    public void checkFollowStatus(String currentUser, String targetUser, FollowCheckCallback callback) {
        Log.d("FollowDebug", "Checking if " + currentUser + " follows " + targetUser);
        db.collection("users")
                .document(currentUser)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.e("FollowDebug", " " + currentUser + " does not exist in Firestore.");
                        callback.onFollowChecked(false);
                    } else {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        boolean isFollowing = (following != null && following.contains(targetUser));
                        Log.d("FollowDebug", " " + currentUser + " following list: " + following);
                        callback.onFollowChecked(isFollowing);
                    }
                })
                .addOnFailureListener(e -> callback.onFollowChecked(false));
    }

    /**
     * Checks if a follow request already exists between two users.
     *
     * @param requester The user who sent the follow request.
     * @param requested The user who received the follow request.
     * @param callback  The callback to handle the result.
     */
    public void checkFollowRequest(String requester, String requested, FollowRequestCheckCallback callback) {
        db.collection("follow_requests")
                .whereEqualTo("requester", requester)
                .whereEqualTo("requested", requested)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onFollowRequestChecked(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> callback.onFollowRequestChecked(false));
    }

    /**
     * Sends a follow request from one user to another.
     *
     * @param requester The user sending the follow request.
     * @param requested The user receiving the follow request.
     * @param callback  The callback to handle the result.
     */
    public void sendFollowRequest(String requester, String requested, FollowRequestActionCallback callback) {
        Log.d("FollowDebug", "Attempting to send follow request: " + requester + " -> " + requested);

        // First, check if a request already exists
        checkFollowRequest(requester, requested, exists -> {
            if (exists) {
                Log.d("FollowDebug", "Follow request already exists, skipping duplicate.");
                callback.onFollowRequestAction(false); // Or provide a separate status for "already exists"
            } else {
                HashMap<String, Object> requestData = new HashMap<>();
                requestData.put("requester", requester);
                requestData.put("requested", requested);
                requestData.put("timestamp", System.currentTimeMillis());

                db.collection("follow_requests")
                        .add(requestData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("FollowDebug", "Follow request sent from " + requester + " to " + requested);
                            callback.onFollowRequestAction(true);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FollowDebug", "Failed to send follow request.", e);
                            callback.onFollowRequestAction(false);
                        });
            }
        });
    }

    /**
     * Retrieves a list of incoming follow requests for a user.
     *
     * @param username The user receiving the follow requests.
     * @param callback The callback to handle the result.
     */
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

    /**
     * Retrieves a list of pending follow requests sent by a user.
     *
     * @param requesterUsername The user who sent the follow requests.
     * @param callback          The callback to handle the result.
     */
    public void getPendingFollowRequests(String requesterUsername, FollowRequestListCallback callback) {
        db.collection("follow_requests")
                .whereEqualTo("requester", requesterUsername)
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

    /**
     * Removes a specific follow request from Firestore.
     *
     * @param requester The user who sent the request.
     * @param requested The user who received the request.
     */
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

    /**
     * Retrieves the list of users that a given user is following.
     *
     * @param username The user whose following list is to be fetched.
     * @param callback The callback to handle the result.
     */
    public void getUserFollowing(String username, UserListCallback callback) {
        Log.d("FollowDebug", "Fetching following list for " + username);
        db.collection("users").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        Log.d("FollowDebug", " " + username + " is following: " + following);
                        callback.onUserListRetrieved(following != null ? following : new ArrayList<>());
                    } else {
                        Log.e("FollowDebug", " " + username + " does not exist in Firestore.");
                        callback.onUserListRetrieved(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowDebug", " Failed to get following list for " + username, e);
                    callback.onUserListRetrieved(new ArrayList<>());
                });
    }

    /**
     * Accepts a follow request, adding the requester to the requested user's followers and the requested user to the requester's following list.
     *
     * @param requester The user who sent the follow request.
     * @param requested The user who received the follow request.
     * @param callback  The callback to handle the result.
     */
    public void acceptFollowRequest(String requester, String requested, FollowActionCallback callback) {
        Log.d("FollowDebug", "Accepting follow request: " + requester + " wants to follow " + requested);

        db.collection("users").document(requested)
                .update("followers", FieldValue.arrayUnion(requester))
                .addOnSuccessListener(aVoid -> {
                    Log.d("FollowDebug", requested + " followers updated: added " + requester);

                    // Add requested user to requester's "following" array
                    db.collection("users").document(requester)
                            .update("following", FieldValue.arrayUnion(requested))
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("FollowDebug", requester + " following updated: added " + requested);
                                // Check database after update
                                db.collection("users").document(requester).get()
                                        .addOnSuccessListener(doc -> {
                                            List<String> following = (List<String>) doc.get("following");
                                            Log.d("FollowDebug", "After update, " + requester + " is following: " + following);
                                        });

                                db.collection("users").document(requested).get()
                                        .addOnSuccessListener(doc -> {
                                            List<String> followers = (List<String>) doc.get("followers");
                                            Log.d("FollowDebug", "After update, " + requested + " has followers: " + followers);
                                        });

                                removeFollowRequest(requester, requested);
                                removeNotification(requester, requested);
                                callback.onFollowAction(true);
                            })
                            .addOnFailureListener(e -> callback.onFollowAction(false));
                })
                .addOnFailureListener(e -> callback.onFollowAction(false));
    }

    /**
     * Removes a notification for a user.
     *
     * @param requester The user who sent the follow request.
     * @param requested The user who received the follow request.
     */
    public void removeNotification(String requester, String requested) {
        Log.d("FollowDebug", "Removing notification for " + requested + " about " + requester + "'s request.");
        db.collection("notifications")
                .whereEqualTo("recipient", requested)
                .whereEqualTo("message", requester + " wants to follow your mood events.")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        db.collection("notifications").document(doc.getId()).delete()
                                .addOnSuccessListener(aVoid -> Log.d("FollowDebug", "Notification deleted: " + doc.getId()))
                                .addOnFailureListener(e -> Log.e("FollowDebug", "Failed to delete notification: " + doc.getId(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e("FollowDebug", "Failed to find notifications for " + requested, e));
    }

    /**
     * Callback interface for checking user existence.
     */
    public interface UserCheckCallback {
        void onUserChecked(boolean exists, String name, String username, String password);
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

    /**
     * Callback interface for fetching a list of follow requests.
     */
    public interface FollowRequestListCallback {
        void onFollowRequestsFetched(List<String> requests);
    }

    /**
     * Callback interface for retrieving a list of users.
     */
    public interface UserListCallback {
        void onUserListRetrieved(List<String> users);
    }
}


package com.example.superior_intelligence;

import android.widget.Button;

/**
 * A helper class that manages follow status between users and updates the follow button UI.
 * Used when viewing someone else's profile to check if you're following them,
 * have sent a request, or haven't interacted yet.
 */
public class FollowManager {

    /**
     * The different types of follow states between two users.
     */
    public enum FollowStatus {
        FOLLOWING,
        REQUEST_SENT,
        NOT_FOLLOWING
    }

    /**
     * A simple callback to return the result of checking follow status.
     */
    public interface StatusCallback {
        void onStatusDetermined(FollowStatus status);
    }


    /**
     * Checks the relationship between the current user and the person they’re viewing.
     * It figures out if you're already following them, if you've sent a request,
     * or if there’s no connection yet.
     * @param currentUser The user who is logged in.
     * @param targetUser  The user whose profile is being viewed.
     * @param userbase    The user database (where we check follow data).
     * @param callback    What to do once we know the follow status.
     */
    public static void checkFollowStatus(String currentUser, String targetUser, Userbase userbase, StatusCallback callback) {
        if (currentUser == null || targetUser == null) {
            callback.onStatusDetermined(FollowStatus.NOT_FOLLOWING);
            return;
        }

        userbase.getUserFollowing(currentUser, followingList -> {
            if (followingList.contains(targetUser)) {
                callback.onStatusDetermined(FollowStatus.FOLLOWING);
            } else {
                userbase.checkFollowRequest(currentUser, targetUser, isRequestSent -> {
                    FollowStatus status = isRequestSent ? FollowStatus.REQUEST_SENT : FollowStatus.NOT_FOLLOWING;
                    callback.onStatusDetermined(status);
                });
            }
        });
    }

    /**
     * Updates the text and clickable state of the follow button based on the current follow status.
     * For example: “Unfollow” if already following, “Pending Request” if a request is sent, or “Follow” if not following.
     * @param button The follow button to update.
     * @param status The current follow status between users.
     */
    public static void updateFollowButton(Button button, FollowStatus status) {
        if (button == null || status == null) return;

        switch (status) {
            case FOLLOWING:
                button.setText("Unfollow");
                button.setEnabled(true);
                break;
            case REQUEST_SENT:
                button.setText("Pending Request");
                button.setEnabled(false);
                break;
            case NOT_FOLLOWING:
                button.setText("Follow");
                button.setEnabled(true);
                break;
        }
    }
}

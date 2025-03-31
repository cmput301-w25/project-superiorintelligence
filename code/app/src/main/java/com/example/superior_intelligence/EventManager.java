package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages event-related functionalities such as retrieving emoji resources,
 * overlay colors, parsing comments, and handling event updates and deletions.
 */
public class EventManager {

    /**
     * Returns a drawable resource ID based on the specified mood.
     *
     * @param mood The mood string (e.g., "happiness", "anger").
     * @return The corresponding drawable resource ID.
     */
    public static int getEmojiResource(String mood) {
        if (mood == null) return R.drawable.happy_icon;
        switch (mood.toLowerCase()) {
            case "anger": return R.drawable.angry_icon;
            case "happiness": return R.drawable.happy_icon;
            case "sadness": return R.drawable.sad_icon;
            case "disgust": return R.drawable.disgust;
            case "confusion": return R.drawable.confusion;
            case "fear": return R.drawable.fear;
            case "shame": return R.drawable.shame;
            case "surprise": return R.drawable.surprise;
            default: return R.drawable.happy_icon;
        }
    }

    /**
     * Returns a hex color string for overlay based on the specified mood.
     *
     * @param mood The mood string.
     * @return A hex color string representing the mood overlay color.
     */
    public static String getOverlayColorForMood(String mood) {
        if (mood == null) return "#FFD700"; // Default to Yellow
        switch (mood.toLowerCase()) {
            case "anger": return "#FF6347"; // Tomato Red
            case "happiness": return "#FFD700"; // Yellow
            case "sadness": return "#87CEEB"; // Sky Blue
            case "fear": return "#778899"; // Slate Gray
            case "shame": return "#FFB6C1"; // Light Pink
            case "confusion": return "#CC0099"; // Purple
            case "surprise": return "#FFA500"; // Orange
            case "disgust": return "#98FB98"; // Pale Green
            default: return "#FFD700"; // Default
        }
    }

    /**
     * Parses a Firestore comment data structure into a list of Comment objects.
     *
     * @param commentsMap A map containing usernames as keys and lists of comment data as values.
     * @return A list of Comment objects extracted from the map.
     */
    public static List<Comment> parseComments(Map<String, List<Map<String, String>>> commentsMap) {
        List<Comment> commentsList = new ArrayList<>();
        if (commentsMap == null) return commentsList;

        for (String user : commentsMap.keySet()) {
            List<Map<String, String>> userComments = commentsMap.get(user);
            for (Map<String, String> commentData : userComments) {
                commentsList.add(new Comment(
                        user,
                        commentData.get("text"),
                        commentData.get("date")
                ));
            }
        }
        return commentsList;
    }

    /**
     * Callback interface for handling event deletion results.
     */
    public interface DeleteCallback {
        void onDeleteResult(boolean success);
    }

    /**
     * Callback interface for handling event update results.
     */
    public interface UpdateCallback {
        void onUpdateResult(boolean success);
    }

    /**
     * Deletes an event from the database.
     *
     * @param db The database instance.
     * @param eventId The ID of the event to be deleted.
     * @param callback The callback to handle the result.
     */
    public void deleteEvent(Database db, String eventId, DeleteCallback callback) {
        db.deleteEvent(eventId, callback::onDeleteResult);
    }

    /**
     * Updates the public/private status of an event in the database.
     *
     * @param db The database instance.
     * @param event The event object to be updated.
     * @param newStatus The new public/private status.
     * @param callback The callback to handle the update result.
     */
    public void updateEventStatus(Database db, Event event, boolean newStatus, UpdateCallback callback) {
        if (event.isPublic_status() != newStatus) {
            event.setPublic_status(newStatus);
            db.updateEvent(event, callback::onUpdateResult);
        } else {
            callback.onUpdateResult(false); // No update needed
        }
    }

}

package com.example.superior_intelligence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventManager {

    /**
     * Returns a drawable resource ID based on mood.
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
     * Returns a hex overlay color string for the given mood.
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

    public interface DeleteCallback {
        void onDeleteResult(boolean success);
    }

    public interface UpdateCallback {
        void onUpdateResult(boolean success);
    }

    public void deleteEvent(Database db, String eventId, DeleteCallback callback) {
        db.deleteEvent(eventId, callback::onDeleteResult);
    }

    public void updateEventStatus(Database db, Event event, boolean newStatus, UpdateCallback callback) {
        if (event.isPublic_status() != newStatus) {
            event.setPublic_status(newStatus);
            db.updateEvent(event, callback::onUpdateResult);
        } else {
            callback.onUpdateResult(false); // No update needed
        }
    }

}

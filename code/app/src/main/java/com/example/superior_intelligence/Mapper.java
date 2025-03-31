package com.example.superior_intelligence;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts between Firestore docs/Map and our Event model.
 */
public class Mapper {

    /**
     * Converts an Event object to a Map representation suitable for database storage.
     * The resulting Map contains all significant fields of the Event object
     * @param event The Event object to convert
     * @return A Map containing all event properties, or empty Map if event is null
     */
    public static Map<String, Object> eventToMap(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.getID()); // Include event ID
        eventData.put("title", event.getTitle());
        eventData.put("date", event.getDate()); // store as string
        eventData.put("overlayColor", event.getOverlayColor());
        eventData.put("imageUrl", event.getImageUrl());
        eventData.put("emojiResource", event.getEmojiResource());
        eventData.put("isFollowed", event.isFollowed());
        eventData.put("isMyPost", event.isMyPost());
        eventData.put("mood", event.getMood());
        eventData.put("situation", event.getSituation());
        eventData.put("moodExplanation", event.getMoodExplanation());
        eventData.put("postUser", event.getPostUser());
        eventData.put("lat", event.getLat());
        eventData.put("lng", event.getLng());
        eventData.put("public_status", event.isPublic_status());
        return eventData;
    }

    /**
     * Converts a Firestore DocumentSnapshot to an Event object.
     * Handles type conversion and null checks for all event fields. Supports multiple
     * date formats (String or Timestamp). Returns null if conversion fails.
     * @param document The Firestore document to convert
     * @return The reconstructed Event object, or null if conversion failed
     * @throws Exception if document parsing encounters unexpected data types
     */
    public static Event docToEvent(DocumentSnapshot document) {
        try {
            String id = document.getString("id");
            String title = document.getString("title");
            Object rawDate = document.get("date");
            String overlayColor = document.getString("overlayColor");
            String imageUrl = document.getString("imageUrl");
            Long longEmoji = document.getLong("emojiResource");
            int emojiResource = (longEmoji != null) ? longEmoji.intValue() : 0;
            Boolean followedBool = document.getBoolean("isFollowed");
            boolean isFollowed = (followedBool != null) ? followedBool : false;
            Boolean myPostBool = document.getBoolean("isMyPost");
            boolean isMyPost = (myPostBool != null) ? myPostBool : true;
            String mood = document.getString("mood");
            String situation = document.getString("situation");
            String moodExplanation = document.getString("moodExplanation");
            String postUser = document.getString("postUser");
            boolean publicStat = document.getBoolean("public_status");
            String dateString = "Unknown Date";
            if (rawDate instanceof String) {
                dateString = (String) rawDate;
            } else if (rawDate instanceof Timestamp) {
                dateString = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(((Timestamp) rawDate).toDate());
            }

            Double lat = document.getDouble("lat");
            Double lng = document.getDouble("lng");

            return new Event(
                    id,
                    title,
                    dateString,
                    overlayColor,
                    imageUrl,
                    emojiResource,
                    isFollowed,
                    isMyPost,
                    mood,
                    moodExplanation,
                    situation,
                    postUser,
                    lat,
                    lng,
                    publicStat
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
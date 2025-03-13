package com.example.superior_intelligence;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.google.firebase.Timestamp;

/**
 * Represents a mood event that a user can create, store, and retrieve from Firestore.
 * Includes properties like title, date, overlay color, mood, and situation.
 */
public class Event implements Serializable {
    private String id;
    private String title;
    private Object date;
    private String overlayColor;
    private String imageUrl;
    private int emojiResource;
    private boolean isFollowed;
    private boolean isMyPost;
    private String mood;
    private String situation;
    private String moodExplanation;
    private String user;
    private Double lng;
    private Double lat;


    /**
     * Empty constructor required for Firestore
     */
    public Event() {
    }

    /**
     * Constructor for creating an event with basic details.
     *
     * @param id            The unique ID of event.
     * @param title         The title of the event.
     * @param date          The date of the event as a String.
     * @param overlayColor  The color overlay associated with the event.
     * @param imageUrl      The URL of the image associated with the event.
     * @param emojiResource The emoji resource ID for the mood.
     * @param isFollowed    Whether the event is followed by the user.
     * @param isMyPost      Whether the event was created by the user.
     * @param user          The username of the event creator.
     */
    public Event(String id, String title, String date, String overlayColor, String imageUrl, int emojiResource, boolean isFollowed, boolean isMyPost, String user, Double lat, Double lng) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.overlayColor = overlayColor;
        this.imageUrl = imageUrl;
        this.emojiResource = emojiResource;
        this.isFollowed = isFollowed;
        this.isMyPost = isMyPost;
        this.user = user;
        this.lat = lat;
        this.lng = lng;
    }
  
    /**
     * Constructor for creating an event with additional mood and situation details.
     *
     * @param title          The title of the event.
     * @param date           The date of the event as a String.
     * @param overlayColor   The color overlay associated with the event.
     * @param imageUrl       The URL of the image associated with the event.
     * @param emojiResource  The emoji resource ID for the mood.
     * @param isFollowed     Whether the event is followed by the user.
     * @param isMyPost       Whether the event was created by the user.
     * @param mood           The mood associated with the event.
     * @param moodExplanation The explanation for the mood.
     * @param situation      The social situation during the event.
     * @param user           The username of the event creator.
     */
    public Event(String id, String title, String date, String overlayColor, String imageUrl, int emojiResource, boolean isFollowed, boolean isMyPost, String mood, String moodExplanation, String situation, String user, Double lat, Double lng) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.overlayColor = overlayColor;
        this.imageUrl = imageUrl;
        this.emojiResource = emojiResource;
        this.isFollowed = isFollowed;
        this.isMyPost = isMyPost;
        this.situation = situation;
        this.mood = mood;
        this.moodExplanation = moodExplanation;
        this.user = user;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * Gets the title of the event.
     * @return The event title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the date of the event.
     * If the date is stored as a `Timestamp`, it is converted into a formatted string.
     * @return The event date as a formatted string.
     */
    public String getDate() {
        if (date instanceof String) {
            return (String) date;
        } else if (date instanceof Timestamp) {
            // Convert Timestamp to Date and then format it as a readable string
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(((Timestamp) date).toDate());
        }
        return "Unknown Date";
    }

    /**
     * Gets the overlay color associated with the event.
     * @return The overlay color.
     */
    public String getOverlayColor() {
        return overlayColor;
    }

    /**
     * Gets the image URL for the event.
     * @return The image URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Gets the emoji resource ID representing the event's mood.
     * @return The emoji resource ID.
     */
    public int getEmojiResource() {
        return emojiResource;
    }

    /**
     * Checks if the event is followed by the user.
     * @return `true` if followed, `false` otherwise.
     */
    public boolean isFollowed() {
        return isFollowed;
    }

    /**
     * Checks if the event was created by the user.
     * @return `true` if it is the user's post, `false` otherwise.
     */
    public boolean isMyPost() {
        return isMyPost;
    }


    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }


    /**
     * Sets the event title.
     * @param title The title to set.
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the event date.
     * @param date The date to set.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the overlay color for the event.
     * @param overlayColor The color to set.
     */
    public void setOverlayColor(String overlayColor) {
        this.overlayColor = overlayColor;
    }

    /**
     * Sets the image URL for the event.
     * @param imageUrl The image URL to set.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Sets the emoji resource ID for the event.
     * @param emojiResource The emoji resource ID to set.
     */
    public void setEmojiResource(int emojiResource) {
        this.emojiResource = emojiResource;
    }

    /**
     * Sets whether the event is followed by the user.
     * @param followed `true` if followed, `false` otherwise.
     */
    public void setFollowed(boolean followed) {
        this.isFollowed = followed;
    }

    /**
     * Sets whether the event was created by the user.
     * @param myPost `true` if the event belongs to the user, `false` otherwise.
     */
    public void setMyPost(boolean myPost) {
        this.isMyPost = myPost;
    }

    /**
     * Gets the mood explanation for the event.
     * @return The mood explanation.
     */
    public String getMoodExplanation() {
        return moodExplanation;
    }

    /**
     * Sets the mood explanation for the event.
     * @param moodExplanation The mood explanation.
     */
    public void setMoodExplanation(String moodExplanation) {
        this.moodExplanation = moodExplanation;
    }

    /**
     * Gets the mood associated with the event.
     * @return The mood.
     */
    public String getMood() {
        return mood;
    }

    /**
     * Sets the mood of the event.
     * @param mood The mood to set.
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * Gets the social situation for the event.
     * @return The social situation.
     */
    public String getSituation() {
        return situation;
    }

    /**
     * Sets the social situation for the event.
     * @param situation The situation to set.
     */
    public void setSituation(String situation) {
        this.situation = situation;
    }

    /**
     * Gets the username of the event creator.
     * @return The username.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the username of the event creator.
     * @param user The username to set.
     */
    public void setUser(String user) {
        this.user = user;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getID(){
        return this.id;
    }
}


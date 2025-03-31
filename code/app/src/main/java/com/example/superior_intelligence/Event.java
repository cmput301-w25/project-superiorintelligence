package com.example.superior_intelligence;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
    private String postUser;
    private Double lng;
    private Double lat;
    private List<Comment> comments;
    private boolean public_status;
    private long timestamp;
    /**
     * Empty constructor requid for Firestore
     */
    public Event() {
        this.comments = new ArrayList<>();
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
     * @param isMyPost      Whether the event was created by the user.25
     * @param postUser          The username of the event creator.
     * @param public_status True if the post is public, otherwise private.
     */
    public Event(String id, String title, String date, String overlayColor, String imageUrl, int emojiResource, boolean isFollowed, boolean isMyPost, String postUser, Double lat, Double lng, List<Comment> comments, boolean public_status) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.overlayColor = overlayColor;
        this.imageUrl = imageUrl;
        this.emojiResource = emojiResource;
        this.isFollowed = isFollowed;
        this.isMyPost = isMyPost;
        this.postUser = postUser;
        this.lat = lat;
        this.lng = lng;
        this.comments = (comments != null) ? comments : new ArrayList<>();
        this.public_status = public_status;
    }
  
    /**
     * Constructor for creating an event with additional mood and situation details.
     *
     * @param id             The unique ID for mood events.
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
     * @param postUser           The username of the event creator.
     * @param public_status  Post status, true if post is public, false otherwise
     */
    public Event(String id, String title, String date, String overlayColor, String imageUrl, int emojiResource, boolean isFollowed, boolean isMyPost, String mood, String moodExplanation, String situation, String postUser, Double lat, Double lng, boolean public_status) {
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
        this.postUser = postUser;
        this.lat = lat;
        this.lng = lng;
        this.public_status = public_status;
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


    /**
     * gets the timestamp of the mood event from what is stored in firebase.
     * @return current timestamp as long int
     */
    public long getTimestamp() { return timestamp;}

    /**
     * set the timestamp to the specific time
     * @param timestamp timestamp to be set
     */
    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}


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
    public String getPostUser() {
        return this.postUser;
    }

    /**
     * Sets the username of the event creator.
     * @param postUser The username to set.
     */
    public void setPostUser(String postUser) {
        this.postUser = postUser;
    }

    /**
     * Sets the latitude of the event location.
     *
     * @param lat The latitude value.
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * Sets the longitude of the event location.
     *
     * @param lng The longitude value.
     */
    public void setLng(Double lng) {
        this.lng = lng;
    }

    /**
     * Sets the unique identifier for the event.
     *
     * @param id The event ID.
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Retrieves the unique identifier of the event.
     *
     * @return The event ID.
     */
    public String getID() {
        return this.id;
    }

    /**
     * Retrieves the list of comments associated with the event.
     *
     * @return A list of comments.
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Sets the comments for the event.
     *
     * @param comments The list of comments to associate with the event.
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * Adds a new comment to the event.
     *
     * @param comment The comment to be added.
     */
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }

    /**
     * Sets the event's public status.
     *
     * @param public_status {@code true} if the event is public, {@code false} otherwise.
     */
    public void setPublic_status(boolean public_status) {
        this.public_status = public_status;
    }

    /**
     * Checks if the event is public.
     *
     * @return {@code true} if the event is public, {@code false} otherwise.
     */
    public boolean isPublic_status() {
        return public_status;
    }


}


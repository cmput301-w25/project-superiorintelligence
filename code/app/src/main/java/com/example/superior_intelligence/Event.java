package com.example.superior_intelligence;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.google.firebase.Timestamp;

public class Event implements Serializable {
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

    public Event() {
        // empty constructor for Firestore
    }

    public Event(String title, String date, String overlayColor, String imageUrl, int emojiResource, boolean isFollowed, boolean isMyPost) {
        this.title = title;
        this.date = date;
        this.overlayColor = overlayColor;
        this.imageUrl = imageUrl;
        this.emojiResource = emojiResource;
        this.isFollowed = isFollowed;
        this.isMyPost = isMyPost;
    }

    public Event(String title, String date, String overlayColor, String imageUrl, int emojiResource, boolean isFollowed, boolean isMyPost, String mood, String moodExplanation, String situation) {
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
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        if (date instanceof String) {
            return (String) date;
        } else if (date instanceof Timestamp) {
            // Convert Timestamp to Date and then format it as a readable string
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(((Timestamp) date).toDate());
        }
        return "Unknown Date";
    }

    public String getOverlayColor() {
        return overlayColor;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getEmojiResource() {
        return emojiResource;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public boolean isMyPost() {
        return isMyPost;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setOverlayColor(String overlayColor) {
        this.overlayColor = overlayColor;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setEmojiResource(int emojiResource) {
        this.emojiResource = emojiResource;
    }

    public void setFollowed(boolean followed) {
        this.isFollowed = followed;
    }

    public void setMyPost(boolean myPost) {
        this.isMyPost = myPost;
    }

    public String getMoodExplanation() {
        return moodExplanation;
    }

    public void setMoodExplanation(String moodExplanation) {
        this.moodExplanation = moodExplanation;
    }

    public String getMood() {
        return mood;
    }

    public void setMood() {
        this.mood = mood;
    }

    public String getSituation() {
        return situation;
    }
    public void setSituation() {
        this.situation = situation;
    }
}

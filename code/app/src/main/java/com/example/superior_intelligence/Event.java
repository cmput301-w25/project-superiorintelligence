package com.example.superior_intelligence;

import java.io.Serializable;

public class Event implements Serializable {
    private String title;
    private String date;
    private String overlayColor;
    private String imageUrl;
    private int emojiResource;
    private boolean isFollowed;
    private boolean isMyPost;

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

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
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
}

package com.example.superior_intelligence;

public class Event {
    private String title;
    private String date;
    private String overlayColor;
    private String imageUrl;
    private int emojiResource;
    private boolean isFollowed;
    private boolean isMyPost;

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

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }
}

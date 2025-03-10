package com.example.superior_intelligence;

import java.io.Serializable;

public class Mood implements Serializable {

    //attributes
    private String moodTitle;
    private String moodExplanation;
    private String emotionalState;
    private long timestamp;
    private String imageUrl;
    private boolean isPost;
    private String mood;
    private String date;

    private String situation;
    private String postUser;
    private String overlayColor;


    //constructors
    public Mood(String moodTitle){
        this.moodTitle = moodTitle;
    }
    //THIS CONSTRUCTOR TO BE UPDATED WITH ALL ATTRIBUTES
    public Mood(String moodTitle, String moodExplanation, String emotionalState){
        this.moodTitle = moodTitle;
        this.moodExplanation = moodExplanation;
        this.emotionalState = emotionalState;
    }

    public String getImageUrl(){  return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
    public boolean isPost() {return isPost;   }
    public void setPost(boolean isPost){ this.isPost = isPost;}

    public String getSituation() {return situation;}
    public void setSituation(String situation){this.situation = situation;}


    public String getMoodTitle() {
        return moodTitle;
    }

    public void setMoodTitle(String moodTitle) {
        this.moodTitle = moodTitle;
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

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostUser() {
        return postUser;
    }

    public void setPostUser(String postUser) {
        this.postUser = postUser;
    }

    public String getOverlayColor() {
        return overlayColor;
    }
    public void setOverlayColor(String overlayColor) {
        this.overlayColor = overlayColor;
    }

}

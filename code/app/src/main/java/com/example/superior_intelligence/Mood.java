package com.example.superior_intelligence;

import java.io.Serializable;

public class Mood implements Serializable {

    //attributes
    private String moodTitle;
    private String moodExplanation;
    private String emotionalState;
    private String socialSituation;
    private long timestamp;

    //constructors
    public Mood(String moodTitle){
        this.moodTitle = moodTitle;
    }
    //THIS CONSTRUCTOR TO BE UPDATED WITH ALL ATTRIBUTES
    public Mood(String moodTitle, String emotionalState, String moodExplanation, String socialSituation) {
        this.moodTitle = moodTitle;
        this.emotionalState = emotionalState;
        this.moodExplanation = moodExplanation;
        this.socialSituation = socialSituation;
    }

    public String getMoodTitle() {
        return moodTitle;
    }

    public void setMoodTitle(String moodTitle) {
        this.moodTitle = moodTitle;
    }

    public String getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    public String getMoodExplanation() {
        return moodExplanation;
    }

    public void setMoodExplanation(String moodExplanation) {
        this.moodExplanation = moodExplanation;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }
}

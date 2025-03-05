package com.example.superior_intelligence;

import java.io.Serializable;

public class Mood implements Serializable {

    //attributes
    private String moodExplanation;
    private String emotionalState;
    private long timestamp;

    public Mood(){

    }

    public String getMoodExplanation() {
        return moodExplanation;
    }

    public void setMoodExplanation(String moodExplanation) {
        this.moodExplanation = moodExplanation;
    }
}

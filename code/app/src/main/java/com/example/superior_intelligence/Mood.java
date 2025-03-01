package com.example.superior_intelligence;

import java.io.Serial;
import java.io.Serializable;

public class Mood implements Serializable {

    //attributes
    private String moodExplaination;

    public String getMoodExplaination() {
        return moodExplaination;
    }

    public void setMoodExplaination(String moodExplaination) {
        this.moodExplaination = moodExplaination;
    }
}

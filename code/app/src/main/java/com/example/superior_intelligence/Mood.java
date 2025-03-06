package com.example.superior_intelligence;

import java.io.Serializable;

/**
 * Represents a Mood event with required attributes like id, timestamp,
 * and emotional state, along with optional attributes like social situation
 * and mood explanation.
 */
public class Mood implements Serializable {

    private final String id; // Unique identifier for the mood event (compulsory)
    private final long timestamp; // Timestamp of mood event creation (compulsory)
    private final String emotionalState; // User's emotional state (compulsory)
    private String socialSituation; // Social context when the mood was recorded (optional)
    private String moodExplanation; // Explanation of the mood event (optional)

    /**
     * Constructor for creating a Mood object with compulsory attributes.
     *
     * @param id             Unique identifier for the mood event.
     * @param timestamp      Timestamp of mood event creation in milliseconds.
     * @param emotionalState The emotional state associated with this mood event.
     */
    public Mood(String id, long timestamp, String emotionalState) {
        this.id = id;
        this.timestamp = timestamp;
        this.emotionalState = emotionalState;
    }

    /**
     * Gets the unique ID of the mood event.
     * @return the mood event ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the timestamp of when the mood event was created.
     * @return the timestamp in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the emotional state associated with this mood event.
     * @return the emotional state.
     */
    public String getEmotionalState() {
        return emotionalState;
    }

    /**
     * Gets the social situation when the mood event occurred.
     * @return the social situation.
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Sets the social situation for this mood event.
     * @param socialSituation the social situation to set.
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the mood explanation.
     * @return the mood explanation.
     */
    public String getMoodExplanation() {
        return moodExplanation;
    }

    /**
     * Sets the mood explanation.
     * @param moodExplanation the mood explanation to set.
     */
    public void setMoodExplanation(String moodExplanation) {
        this.moodExplanation = moodExplanation;
    }
}

package com.example.superior_intelligence;

import java.io.Serializable;

/**
 * Represents a user comment with a username, text content, and timestamp.
 * Implements Serializable for data persistence and Comparable for sorting by timestamp.
 */
public class Comment implements Serializable, Comparable<Comment>  {

    private String username;
    private String text;
    private String time;

    /**
     * Default constructor for serialization and Firebase operations.
     */
    public Comment() {

    }

    /**
     * Constructs a new Comment instance with the specified username, text, and timestamp.
     * @param username the username of the commenter
     * @param text     the content of the comment
     * @param time     the timestamp when the comment was created, formatted as a string
     */
    public Comment(String username, String text, String time) {
        this.username = username;
        this.text = text;
        this.time = time;
    }

    /**
     * Retrieves the username of the commenter.
     * @return the commenter's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the commenter.
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the content of the comment.
     * @return the comment text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the content of the comment.
     * @param text the text content of the comment to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Retrieves the timestamp of when the comment was created.
     * @return the comment timestamp as a formatted string
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the timestamp of the comment.
     * @param time the timestamp of the comment creation to set, formatted as a string
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Checks if two Comment objects are equal based on username, text, and timestamp.
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment c = (Comment) o;
        return username.equals(c.username) && text.equals(c.text) && time.equals(c.time);
    }

    /**
     * Generates a hash code for the Comment object based on username, text, and timestamp.
     * @return the hash code of the Comment object
     */
    @Override
    public int compareTo(Comment other) {
        return this.time.compareTo(other.time); // For now, compare as strings (dd/MM/yy H:mm)
    }
}

package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the system with profile and relationship data.
 * Serves as a data model for Firestore serialization and user management.
 * Contains basic user information and social graph connections.
 */
public class UserHelper {

    private String name;
    private String username;
    private String password;     // <-- NEW FIELD
    private List<String> followers;
    private List<String> following;

    /**
     * Default constructor required for Firestore serialization.
     */
    public UserHelper() {
    }

    /**
     * Creates a user without password initialization.
     * @param name The user's display name
     * @param username The user's unique identifier
     */
    public UserHelper(String name, String username) {
        this.name = name;
        this.username = username;
        this.password = ""; // default empty password or handle it differently
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    /**
     * Creates a user with all fields including password.
     * @param name The user's display name
     * @param username The user's unique identifier
     * @param password The user's password (should be hashed)
     */
    public UserHelper(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    // Getters and Setters for name
    /**
     * Gets the user's display name.
     * @return The user's name
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the user's display name.
     * @param name The new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    // Getters and Setters for username
    /**
     * Gets the user's unique username.
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    /**
     * Sets the user's username.
     * @param username The new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter for password
    /**
     * Gets the user's password (typically hashed).
     * @return The password value
     */
    public String getPassword() {
        return password;
    }
    /**
     * Sets the user's password.
     * @param password The new password (should be hashed before setting)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    // Getters and Setters for followers
    /**
     * Gets the list of follower usernames.
     * @return List of follower usernames
     */
    public List<String> getFollowers() {
        return followers;
    }
    /**
     * Sets the complete list of followers.
     * @param followers New list of follower usernames
     */
    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    // Getters and Setters for following
    /**
     * Gets the list of usernames this user follows.
     * @return List of followed usernames
     */
    public List<String> getFollowing() {
        return following;
    }
    /**
     * Sets the complete list of followed users.
     * @param following New list of followed usernames
     */
    public void setFollowing(List<String> following) {
        this.following = following;
    }
}

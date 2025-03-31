package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;

public class UserHelper {

    private String name;
    private String username;
    private String password;     // <-- NEW FIELD
    private List<String> followers;
    private List<String> following;

    // Empty constructor for Firestore serialization (required).
    public UserHelper() {
    }

    // Constructor without password (if needed in some parts of your code).
    public UserHelper(String name, String username) {
        this.name = name;
        this.username = username;
        this.password = ""; // default empty password or handle it differently
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    // Full constructor including password.
    public UserHelper(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    // Getters and Setters for name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Getters and Setters for username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter for password
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    // Getters and Setters for followers
    public List<String> getFollowers() {
        return followers;
    }
    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    // Getters and Setters for following
    public List<String> getFollowing() {
        return following;
    }
    public void setFollowing(List<String> following) {
        this.following = following;
    }
}

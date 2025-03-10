package com.example.superior_intelligence;

public class User {
    private static User instance;
    private String name;
    private String username;

    // Private constructor to prevent direct instantiation
    private User() {}

    // Get the singleton instance
    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Method to reset user data (e.g., logout)
    public void clearUserData() {
        name = null;
        username = null;
    }
}

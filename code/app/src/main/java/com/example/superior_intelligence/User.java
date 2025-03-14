/**
 * Used in both CreateAccountActivity and LoginPageActivity
 * User is a singleton class that holds the logged-in user's data globally throughout the app's lifecycle.
 * Purpose: Ensures that the user data persists across different activities without needing to pass it explicitly between them.
 */

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

    /**
     * Method to reset user data
     */
    public void clearUserData() {
        name = null;
        username = null;
    }

    /**
     * Checks if we are currently referencing the current user
     * @param username the user currently logged in.
     */
    public boolean isCurrentUser(String username) {
        return this.username != null && this.username.equals(username);
    }
}

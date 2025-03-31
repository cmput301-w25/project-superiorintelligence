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

    /**
     * Private constructor to prevent direct instantiation
     */
    private User() {}

    /**
     * Get the singleton instance
     * @return instance user instance
     */
    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    /**
     * Retrieve the name of user
     * @return name user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of user
     * @param name name of the user to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieve the username of the user's account
     * @return username account's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username of the account
     * @param username account's unique username
     */
    public void setUsername(String username) {
        this.username = username;
    }

}

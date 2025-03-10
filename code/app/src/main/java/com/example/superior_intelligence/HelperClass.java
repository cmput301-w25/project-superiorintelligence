/**
 * Helper class for CreateAccountActiuvity
 * contains getters and setters for name and username
 * Purpose: Acts as a temporary container for user data to simplify Firestore interaction.
 * It is used primarily for creating and writing user data to Firestore.
 */

package com.example.superior_intelligence;

public class HelperClass {

    String name, username;

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


    public HelperClass(String name, String username) {
        this.name = name;
        this.username = username;

    }

    public HelperClass() {

    }

}

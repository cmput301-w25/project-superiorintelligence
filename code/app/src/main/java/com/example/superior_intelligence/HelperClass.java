/**
 * Helper class for CreateAccountActiuvity
 * contains getters and setters for name and username
 * Purpose: Acts as a temporary container for user data to simplify Firestore interaction.
 * It is used primarily for creating and writing user data to Firestore.
 */

package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;

public class HelperClass {

    String name, username;
    List<String> followers;
    List<String> following;

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
    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> following) { this.following = following; }

    public HelperClass(String name, String username) {
        this.name = name;
        this.username = username;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    public HelperClass() {

    }

}

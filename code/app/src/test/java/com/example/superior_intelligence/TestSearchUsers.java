package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestSearchUsers {

    private List<UserHelper> mockUsers;

    @Before
    public void setUp() {
        mockUsers = new ArrayList<>();
        mockUsers.add(new UserHelper("Alice", "alice"));
        mockUsers.add(new UserHelper("Bob", "bob"));
        mockUsers.add(new UserHelper("Paul", "paul"));
        mockUsers.add(new UserHelper("Pam", "pamela"));
        mockUsers.add(new UserHelper("Susan", "susan"));
    }

    /**
     * Test that searching for "pa" returns only users with usernames starting with "pa".
     */
    @Test
    public void searchPrefix_returnsUsersMatchingPrefix_only() {
        List<UserHelper> results = SearchUserManager.searchPrefix("pa", mockUsers, "bob");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("paul")));
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("pamela")));
    }

    /**
     * Test that the current user's username is excluded from search results.
     */
    @Test
    public void searchPrefix_excludesCurrentUserFromResults() {
        List<UserHelper> results = SearchUserManager.searchPrefix("bo", mockUsers, "bob");

        assertEquals(0, results.size()); // "bob" should be excluded
    }

    /**
     * Test that a prefix with no matching usernames returns an empty list.
     */
    @Test
    public void searchPrefix_returnsEmptyListWhenNoMatches() {
        List<UserHelper> results = SearchUserManager.searchPrefix("xyz", mockUsers, "bob");

        assertTrue(results.isEmpty());
    }

    /**
     * Test that all users are included when current user is not part of the dataset.
     */
    @Test
    public void searchPrefix_returnsCorrectResultsIfCurrentUserAbsent() {
        List<UserHelper> results = SearchUserManager.searchPrefix("pa", mockUsers, "notinthelist");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("paul")));
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("pamela")));
    }
}
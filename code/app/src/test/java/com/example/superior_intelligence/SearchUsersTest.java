package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersTest {

    private List<HelperClass> mockUsers;

    @Before
    public void setUp() {
        mockUsers = new ArrayList<>();
        mockUsers.add(new HelperClass("Alice", "alice"));
        mockUsers.add(new HelperClass("Bob", "bob"));
        mockUsers.add(new HelperClass("Paul", "paul"));
        mockUsers.add(new HelperClass("Pam", "pamela"));
        mockUsers.add(new HelperClass("Susan", "susan"));
    }

    /**
     * Test that searching for "pa" returns only users with usernames starting with "pa".
     */
    @Test
    public void searchPrefix_returnsUsersMatchingPrefix_only() {
        List<HelperClass> results = SearchUserManager.searchPrefix("pa", mockUsers, "bob");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("paul")));
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("pamela")));
    }

    /**
     * Test that the current user's username is excluded from search results.
     */
    @Test
    public void searchPrefix_excludesCurrentUserFromResults() {
        List<HelperClass> results = SearchUserManager.searchPrefix("bo", mockUsers, "bob");

        assertEquals(0, results.size()); // "bob" should be excluded
    }

    /**
     * Test that a prefix with no matching usernames returns an empty list.
     */
    @Test
    public void searchPrefix_returnsEmptyListWhenNoMatches() {
        List<HelperClass> results = SearchUserManager.searchPrefix("xyz", mockUsers, "bob");

        assertTrue(results.isEmpty());
    }

    /**
     * Test that all users are included when current user is not part of the dataset.
     */
    @Test
    public void searchPrefix_returnsCorrectResultsIfCurrentUserAbsent() {
        List<HelperClass> results = SearchUserManager.searchPrefix("pa", mockUsers, "notinthelist");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("paul")));
        assertTrue(results.stream().anyMatch(user -> user.getUsername().equals("pamela")));
    }
}
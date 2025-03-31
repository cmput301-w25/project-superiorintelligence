package com.example.superior_intelligence;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the User singleton class.
 */
public class TestUser {

    private User user;

    @Before
    public void setUp() {
        user = User.getInstance();
        user.setName(null);
        user.setUsername(null);
    }

    @Test
    public void testUserInstance_isNotNull() {
        assertNotNull(user);
    }

    @Test
    public void testUserInstance_isSingleton() {
        User anotherRef = User.getInstance();
        assertSame(user, anotherRef);
    }

    @Test
    public void testUser_setAndGetName() {
        user.setName("Test Name");
        assertEquals("Test Name", user.getName());
    }

    @Test
    public void testUser_setAndGetUsername() {
        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());
    }

    @Test
    public void testUserHelper_basicConstructor() {
        UserHelper helper = new UserHelper("Alice", "alice123");

        assertEquals("Alice", helper.getName());
        assertEquals("alice123", helper.getUsername());
        assertEquals("", helper.getPassword()); // default empty
        assertNotNull(helper.getFollowers());
        assertNotNull(helper.getFollowing());
        assertTrue(helper.getFollowers().isEmpty());
        assertTrue(helper.getFollowing().isEmpty());
    }

    @Test
    public void testUserHelper_fullConstructor() {
        UserHelper helper = new UserHelper("Bob", "bob456", "securepass");

        assertEquals("Bob", helper.getName());
        assertEquals("bob456", helper.getUsername());
        assertEquals("securepass", helper.getPassword());
        assertNotNull(helper.getFollowers());
        assertNotNull(helper.getFollowing());
    }

    @Test
    public void testUserHelper_setters() {
        UserHelper helper = new UserHelper();

        helper.setName("Charlie");
        helper.setUsername("charlie789");
        helper.setPassword("mypassword");

        assertEquals("Charlie", helper.getName());
        assertEquals("charlie789", helper.getUsername());
        assertEquals("mypassword", helper.getPassword());
    }

    @Test
    public void testUserHelper_followLists() {
        UserHelper helper = new UserHelper();

        List<String> followers = Arrays.asList("user1", "user2");
        List<String> following = Arrays.asList("user3");

        helper.setFollowers(followers);
        helper.setFollowing(following);

        assertEquals(2, helper.getFollowers().size());
        assertEquals("user1", helper.getFollowers().get(0));
        assertEquals(1, helper.getFollowing().size());
        assertEquals("user3", helper.getFollowing().get(0));
    }
}

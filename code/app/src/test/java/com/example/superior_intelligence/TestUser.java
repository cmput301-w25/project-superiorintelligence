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

    /**
     * Resets the User singleton before each test.
     */
    @Before
    public void setUp() {
        user = User.getInstance();
        user.setName(null);
        user.setUsername(null);
    }

    /**
     * Checks that the User instance is not null.
     */
    @Test
    public void testUserInstance_isNotNull() {
        assertNotNull(user);
    }

    /**
     * Ensures the User class truly behaves like a singleton.
     * Both references should point to the same instance.
     */
    @Test
    public void testUserInstance_isSingleton() {
        User anotherRef = User.getInstance();
        assertSame(user, anotherRef);
    }

    /**
     * Tests that the name property can be set and retrieved correctly.
     */
    @Test
    public void testUser_setAndGetName() {
        user.setName("Test Name");
        assertEquals("Test Name", user.getName());
    }

    /**
     * Tests that the username property can be set and retrieved correctly.
     */
    @Test
    public void testUser_setAndGetUsername() {
        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());
    }

    /**
     * Verifies default values and structure when using the basic UserHelper constructor.
     */
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

    /**
     * Verifies that the full constructor of UserHelper sets all fields correctly.
     */
    @Test
    public void testUserHelper_fullConstructor() {
        UserHelper helper = new UserHelper("Bob", "bob456", "securepass");

        assertEquals("Bob", helper.getName());
        assertEquals("bob456", helper.getUsername());
        assertEquals("securepass", helper.getPassword());
        assertNotNull(helper.getFollowers());
        assertNotNull(helper.getFollowing());
    }

    /**
     * Checks that UserHelper setters correctly update all core fields.
     */
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

    /**
     * Tests that follower and following lists can be set and accessed properly.
     */
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

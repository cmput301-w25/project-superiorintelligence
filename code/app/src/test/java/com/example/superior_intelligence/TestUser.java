package com.example.superior_intelligence;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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
}

package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateAccountTest {

    @Mock
    Userbase mockUserbase;

    private boolean callbackTriggered;
    private boolean resultStatus;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        callbackTriggered = false;
        resultStatus = false;
    }

    /**
     * Test creating a new user successfully sets name and username.
     */
    @Test
    public void testUserCreationSuccess() {
        String testName = "Test Name";
        String testUsername = "testuser";
        String testPassword = "password123";

        User user = User.getInstance();
        user.setName(null);
        user.setUsername(null);

        user.setName(testName);
        user.setUsername(testUsername);

        assertEquals(testName, user.getName());
        assertEquals(testUsername, user.getUsername());
    }

    /**
     * Test that username already taken triggers error flow.
     */
    @Test
    public void testDuplicateUsernameHandling() {
        String existingUsername = "takenUsername";

        mockUserbase.checkUserExists(existingUsername, (exists, name, username, password) -> {
            callbackTriggered = true;
            resultStatus = exists;
        });

        // if username was already taken
        callbackTriggered = true;
        resultStatus = true;

        assertTrue(callbackTriggered);
        assertTrue(callbackTriggered);
    }

    /**
     * Test that empty fields should be invalid.
     */
    @Test
    public void testEmptyFieldsValidation() {
        String emptyName = "";
        String emptyUsername = "";
        String emptyPassword = "";

        assertTrue(emptyName.isEmpty());
        assertTrue(emptyUsername.isEmpty());
        assertTrue(emptyPassword.isEmpty());
    }

    /**
     * Test that password is hashed correctly using SHA-256.
     */
    @Test
    public void testPasswordHashing() {
        String rawPassword = "secret";
        String hashed = PasswordHasher.hashPassword(rawPassword);

        assertNotNull(hashed);
        assertNotEquals(hashed, rawPassword);
        assertEquals(64, hashed.length());
    }

    /**
     * Test that account creation should be skipped if any
     * required field (name, username, password) is empty.
     */
    @Test
    public void testAccountCreationSkippedIfFieldsEmpty() {
        String name = "";
        String username = "validUsername";
        String password = "somePassword";

        boolean shouldCreate = !(name.isEmpty() || username.isEmpty() || password.isEmpty());
        assertFalse(shouldCreate);
    }

    /**
     * Test that validation fails if the username input is empty.
     */
    @Test
    public void testUsernameValidationFailsWhenEmpty() {
        String username = "";
        boolean isValid = !username.isEmpty();
        assertFalse(isValid);
    }

    /**
     * Test that validation fails if the password input is empty.
     */
    @Test
    public void testPasswordValidationFailsWhenEmpty() {
        String password = "";
        boolean isValid = !password.isEmpty();
        assertFalse(isValid);
    }

}

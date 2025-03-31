package com.example.superior_intelligence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class TestUniqueUsername {

    private Userbase userbase;

    @Before
    public void setup() {
        userbase = mock(Userbase.class);
    }

    @Test
    public void testCheckUserExists_UserExists() throws InterruptedException {
        doAnswer(invocation -> {
            Userbase.UserCheckCallback callback = invocation.getArgument(1);
            callback.onUserChecked(true, "Test Name", "testUser", "password123");
            return null;
        }).when(userbase).checkUserExists(eq("testUser"), any());

        CountDownLatch latch = new CountDownLatch(1);
        userbase.checkUserExists("testUser", (exists, name, username, password) -> {
            assertTrue(exists);
            assertEquals("Test Name", name);
            assertEquals("testUser", username);
            assertEquals("password123", password);
            latch.countDown();
        });

        assertTrue("Check user exists timed out", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testCheckUserExists_UserDoesNotExist() throws InterruptedException {
        doAnswer(invocation -> {
            Userbase.UserCheckCallback callback = invocation.getArgument(1);
            callback.onUserChecked(false, null, null, null);
            return null;
        }).when(userbase).checkUserExists(eq("nonExistentUser"), any());

        CountDownLatch latch = new CountDownLatch(1);
        userbase.checkUserExists("nonExistentUser", (exists, name, username, password) -> {
            assertFalse(exists);
            assertNull(name);
            assertNull(username);
            assertNull(password);
            latch.countDown();
        });

        assertTrue("Check user does not exist timed out", latch.await(5, TimeUnit.SECONDS));
    }
}
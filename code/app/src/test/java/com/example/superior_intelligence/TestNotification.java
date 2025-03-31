package com.example.superior_intelligence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestNotification {

    @Mock
    private Userbase mockUserbase;

    @Mock
    private Context mockContext;

    private String requester = "alice";
    private String requested = "bob";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendFollowRequest_addsRequestSuccessfully() {
        doAnswer(invocation -> {
            Userbase.FollowRequestActionCallback callback = invocation.getArgument(2);
            callback.onFollowRequestAction(true);
            return null;
        }).when(mockUserbase).sendFollowRequest(eq(requester), eq(requested), any(Userbase.FollowRequestActionCallback.class));

        mockUserbase.sendFollowRequest(requester, requested, success -> assertTrue(success));

        verify(mockUserbase).sendFollowRequest(eq(requester), eq(requested), any(Userbase.FollowRequestActionCallback.class));
    }

    @Test
    public void testSendFollowRequest_failure() {
        doAnswer(invocation -> {
            Userbase.FollowRequestActionCallback callback = invocation.getArgument(2);
            callback.onFollowRequestAction(false);  // simulate failure
            return null;
        }).when(mockUserbase).sendFollowRequest(eq(requester), eq(requested), any(Userbase.FollowRequestActionCallback.class));

        mockUserbase.sendFollowRequest(requester, requested, success -> assertFalse(success));
    }

    @Test
    public void testAcceptFollowRequest_failure() {
        doAnswer(invocation -> {
            Userbase.FollowActionCallback callback = invocation.getArgument(2);
            callback.onFollowAction(false);  // simulate failure
            return null;
        }).when(mockUserbase).acceptFollowRequest(eq(requester), eq(requested), any(Userbase.FollowActionCallback.class));

        mockUserbase.acceptFollowRequest(requester, requested, success -> assertFalse(success));
    }

    @Test
    public void testAcceptFollowRequest_succeeds() {
        doAnswer(invocation -> {
            Userbase.FollowActionCallback callback = invocation.getArgument(2);
            callback.onFollowAction(true);
            return null;
        }).when(mockUserbase).acceptFollowRequest(eq(requester), eq(requested), any(Userbase.FollowActionCallback.class));

        mockUserbase.acceptFollowRequest(requester, requested, success -> assertTrue(success));

        verify(mockUserbase).acceptFollowRequest(eq(requester), eq(requested), any(Userbase.FollowActionCallback.class));
    }

    @Test
    public void testGetIncomingFollowRequests_returnsList() {
        doAnswer(invocation -> {
            Userbase.FollowRequestListCallback callback = invocation.getArgument(1);
            callback.onFollowRequestsFetched(Arrays.asList("seth", "dave"));
            return null;
        }).when(mockUserbase).getIncomingFollowRequests(eq("bob"), any(Userbase.FollowRequestListCallback.class));

        mockUserbase.getIncomingFollowRequests("bob", requests -> {
            assertEquals(2, requests.size());
            assertTrue(requests.contains("seth"));
            assertTrue(requests.contains("dave"));
        });

        verify(mockUserbase).getIncomingFollowRequests(eq("bob"), any(Userbase.FollowRequestListCallback.class));
    }

    @Test
    public void testGetIncomingFollowRequests_returnsEmptyList() {
        doAnswer(invocation -> {
            Userbase.FollowRequestListCallback callback = invocation.getArgument(1);
            callback.onFollowRequestsFetched(new ArrayList<>());
            return null;
        }).when(mockUserbase).getIncomingFollowRequests(eq("bob"), any(Userbase.FollowRequestListCallback.class));

        mockUserbase.getIncomingFollowRequests("bob", requests -> {
            assertNotNull(requests);
            assertTrue(requests.isEmpty());
        });
    }

    @Test
    public void testNotificationAdapter_getItemCount() {
        List<String> mockData = Arrays.asList("alice wants to follow you", "bob wants to follow you");
        NotificationAdapter adapter = new NotificationAdapter(mockData, false);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testNotificationAdapter_emptyList() {
        List<String> mockData = Arrays.asList();
        NotificationAdapter adapter = new NotificationAdapter(mockData, false);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testNotificationAdapter_handlesNullUsername() {
        List<String> mockData = Arrays.asList((String) null);
        NotificationAdapter adapter = new NotificationAdapter(mockData, false);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testNotificationAdapter_duplicateUsernames() {
        List<String> mockData = Arrays.asList("alice", "alice");
        NotificationAdapter adapter = new NotificationAdapter(mockData, false);
        assertEquals(2, adapter.getItemCount());
    }
}

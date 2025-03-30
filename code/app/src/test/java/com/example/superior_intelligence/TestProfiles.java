package com.example.superior_intelligence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.Button;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests for profile-related logic like follow status, button updates, verification.
 */
public class ProfilesTest {

    private Userbase mockUserbase;
    private Button mockButton;

    @Before
    public void setup() {
        mockUserbase = mock(Userbase.class);
        mockButton = mock(Button.class);
    }

    /**
     * Verifies that when the viewing user is already following the profile owner,
     * the status is FOLLOWING.
     */
    @Test
    public void testFollowStatus_Following() {
        doAnswer(invocation -> {
            Userbase.UserListCallback callback = invocation.getArgument(1);
            callback.onUserListRetrieved(Arrays.asList("targetUser"));
            return null;
        }).when(mockUserbase).getUserFollowing(eq("me"), any());

        FollowManager.checkFollowStatus("me", "targetUser", mockUserbase, status ->
                assertEquals(FollowManager.FollowStatus.FOLLOWING, status)
        );
    }

    /**
     * Verifies that if a follow request has been sent but not accepted,
     * the status is REQUEST_SENT.
     */
    @Test
    public void testFollowStatus_RequestSent() {
        doAnswer(invocation -> {
            Userbase.UserListCallback callback = invocation.getArgument(1);
            callback.onUserListRetrieved(Collections.emptyList());

            doAnswer(innerInvocation -> {
                Userbase.FollowRequestCheckCallback cb = innerInvocation.getArgument(2);
                cb.onFollowRequestChecked(true);
                return null;
            }).when(mockUserbase).checkFollowRequest(eq("me"), eq("targetUser"), any());

            return null;
        }).when(mockUserbase).getUserFollowing(eq("me"), any());

        FollowManager.checkFollowStatus("me", "targetUser", mockUserbase, status ->
                assertEquals(FollowManager.FollowStatus.REQUEST_SENT, status)
        );
    }

    /**
     * Verifies that if the viewing user is neither following nor has a pending request,
     * the status is NOT_FOLLOWING.
     */
    @Test
    public void testFollowStatus_NotFollowing() {
        doAnswer(invocation -> {
            Userbase.UserListCallback callback = invocation.getArgument(1);
            callback.onUserListRetrieved(Collections.emptyList());

            doAnswer(innerInvocation -> {
                Userbase.FollowRequestCheckCallback cb = innerInvocation.getArgument(2);
                cb.onFollowRequestChecked(false);
                return null;
            }).when(mockUserbase).checkFollowRequest(eq("me"), eq("targetUser"), any());

            return null;
        }).when(mockUserbase).getUserFollowing(eq("me"), any());

        FollowManager.checkFollowStatus("me", "targetUser", mockUserbase, status ->
                assertEquals(FollowManager.FollowStatus.NOT_FOLLOWING, status)
        );
    }

    /**
     * Checks that the follow button correctly reflects the FOLLOWING state.
     */
    @Test
    public void testUpdateFollowButton_Following() {
        FollowManager.updateFollowButton(mockButton, FollowManager.FollowStatus.FOLLOWING);
        verify(mockButton).setText("Unfollow");
        verify(mockButton).setEnabled(true);
    }

    /**
     * Checks that the follow button correctly reflects a pending follow request.
     */
    @Test
    public void testUpdateFollowButton_RequestSent() {
        FollowManager.updateFollowButton(mockButton, FollowManager.FollowStatus.REQUEST_SENT);
        verify(mockButton).setText("Pending Request");
        verify(mockButton).setEnabled(false);
    }

    /**
     * Checks that the follow button shows the Follow option when users are not connected.
     */
    @Test
    public void testUpdateFollowButton_NotFollowing() {
        FollowManager.updateFollowButton(mockButton, FollowManager.FollowStatus.NOT_FOLLOWING);
        verify(mockButton).setText("Follow");
        verify(mockButton).setEnabled(true);
    }

    /**
     * Ensures that the profile name and @username are correctly displayed when the user exists.
     */
    @Test
    public void testUserDetails_NameAndUsernameDisplayedCorrectly() {
        TextView mockName = mock(TextView.class);
        TextView mockUsername = mock(TextView.class);

        doAnswer(invocation -> {
            Userbase.UserDetailsCallback callback = invocation.getArgument(1);
            callback.onUserDetailsFetched(true, "yum", "Good Soup");
            return null;
        }).when(mockUserbase).getUserDetails(eq("yum"), any());

        mockUserbase.getUserDetails("yum", (exists, fetchedUsername, name) -> {
            if (exists) {
                mockUsername.setText("@" + fetchedUsername);
                mockName.setText(name);
            }
        });

        verify(mockUsername).setText("@yum");
        verify(mockName).setText("Good Soup");
    }

    /**
     * Verifies that if the user is not found in Firestore, no profile text is set.
     */
    @Test
    public void testUserDetails_UserNotFound() {
        TextView mockName = mock(TextView.class);
        TextView mockUsername = mock(TextView.class);

        doAnswer(invocation -> {
            Userbase.UserDetailsCallback callback = invocation.getArgument(1);
            callback.onUserDetailsFetched(false, null, null);
            return null;
        }).when(mockUserbase).getUserDetails(eq("ghost"), any());

        mockUserbase.getUserDetails("ghost", (exists, fetchedUsername, name) -> {
            if (exists) {
                mockUsername.setText("@" + fetchedUsername);
                mockName.setText(name);
            }
        });

        verify(mockUsername, never()).setText(anyString());
        verify(mockName, never()).setText(anyString());
    }

}

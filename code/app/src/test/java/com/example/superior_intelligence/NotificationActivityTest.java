package com.example.superior_intelligence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class NotificationActivityTest {

    @Test
    public void testFetchIncomingRequests_populatesRecyclerView() {
        // Arrange
        Userbase mockUserbase = mock(Userbase.class);
        User mockUser = mock(User.class);
        when(mockUser.getUsername()).thenReturn("testuser");

        doAnswer(invocation -> {
            Userbase.FollowRequestListCallback callback = invocation.getArgument(1);
            callback.onFollowRequestsFetched(Arrays.asList("alice", "bob"));
            return null;
        }).when(mockUserbase).getIncomingFollowRequests(eq("testuser"), any());

        // Launch activity
        ActivityScenario<NotificationActivity> scenario = ActivityScenario.launch(NotificationActivity.class);

        scenario.onActivity(activity -> {
            // Inject mocks
            activity.setUser(mockUser);
            activity.setUserbase(mockUserbase);

            // Act
            activity.fetchIncomingRequests();

            // Assert
            RecyclerView recycler = activity.findViewById(R.id.notifications_recycler_view);
            assertNotNull(recycler.getAdapter());
            assertEquals(2, recycler.getAdapter().getItemCount());
        });
    }
}

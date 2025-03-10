package com.example.superior_intelligence;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HomeActivityTest {

    private HomeActivity homeActivity;
    private TextView mockTabExplore, mockTabFollowed, mockTabMyPosts;

    @Before
    public void setUp() {
        homeActivity = new HomeActivity();
        mockTabExplore = mock(TextView.class);
        mockTabFollowed = mock(TextView.class);
        mockTabMyPosts = mock(TextView.class);
    }

    @Test
    public void testSwitchTabToMyPosts() {
        // Add events to lists
        List<Event> myPosts = new ArrayList<>();
        myPosts.add(new Event("My Post", "01 Jan 2024", "#FFD700", "", 0, false, true, "Happy", "Good day", "Alone", "User"));

        // Switch tab to "MyPosts"
        homeActivity.switchTab(myPosts, mockTabMyPosts);

        // Verify that the correct list is set
        assertEquals(1, homeActivity.adapter.getItemCount());
    }

    @Test
    public void testSwitchTabToExplore() {
        List<Event> exploreList = new ArrayList<>();
        exploreList.add(new Event("Explore Event", "02 Feb 2024", "#FFD700", "", 0, false, false, "Sad", "Bad day", "Crowd", "User"));

        homeActivity.switchTab(exploreList, mockTabExplore);

        assertEquals(1, homeActivity.adapter.getItemCount());
    }
}

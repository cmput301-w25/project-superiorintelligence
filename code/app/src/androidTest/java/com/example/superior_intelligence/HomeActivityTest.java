package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.mockito.Mockito.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    private FirebaseFirestore mockFirestore;
    private CollectionReference mockCollectionRef;
    private ActivityScenario<HomeActivity> scenario;

    @Before
    public void setUp() {
        // Launch the activity
        scenario = ActivityScenario.launch(HomeActivity.class);

        // Mock Firebase Firestore
        mockFirestore = mock(FirebaseFirestore.class);
        mockCollectionRef = mock(CollectionReference.class);
    }

    @Test
    public void testLoadEventsFromFirebase() {
        scenario.onActivity(activity -> {
            activity.db = mockFirestore;
            activity.myPostsRef = mockCollectionRef;

            // Mock Firestore response
            QuerySnapshot mockSnapshot = mock(QuerySnapshot.class);
            Task<QuerySnapshot> mockTask = mock(Task.class);
            when(mockCollectionRef.get()).thenReturn(mockTask);
            when(mockTask.isSuccessful()).thenReturn(true);
            when(mockTask.getResult()).thenReturn(mockSnapshot);

            activity.loadEventsFromFirebase();
            verify(mockCollectionRef, times(1)).get();
        });
    }

    @Test
    public void testSaveEventToFirebase() {
        scenario.onActivity(activity -> {
            activity.db = mockFirestore;
            activity.myPostsRef = mockCollectionRef;

            Event mockEvent = new Event("Test Event", "01 Jan 2024", "#FFFFFF", "", 0, false, true, "Happy", "Good day", "Alone", "User");

            activity.saveEventToFirebase(mockEvent);
            verify(mockCollectionRef, times(1)).add(any(Map.class));
        });
    }

    @Test
    public void testNavigateToMoodCreateActivity() {
        scenario.onActivity(activity -> {
            Intent intent = new Intent(activity, MoodCreateAndEditActivity.class);
            activity.startActivity(intent);

            // Verify that the new activity starts
            onView(withId(R.id.mood_event_title)).check(matches(isDisplayed()));
        });
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
    }
}

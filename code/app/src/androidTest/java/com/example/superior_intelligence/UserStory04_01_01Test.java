package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import org.junit.After;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.concurrent.CountDownLatch;
import android.content.Intent;
import android.os.SystemClock;
import com.google.firebase.firestore.FirebaseFirestore;
import static org.hamcrest.Matchers.is;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.text.SimpleDateFormat;
import org.junit.Before;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.Rule;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import androidx.test.rule.ActivityTestRule;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import androidx.test.ext.junit.runners.AndroidJUnit4;
/**
 * checks the mood history to see if it shows only the current user’s events  and that they are sorted in reverse chronological order (most recent first).
 * - Signs in anonymously and generates a unique test username.
 * - creates two test events for that user and one event for another user.
 * - launch HomeActivity, switch to the MYPOSTS tab, and assert that only two events (for the test user) appear.
 * - also cleans up the test events.
 */
@RunWith(AndroidJUnit4.class)
public class UserStory04_01_01Test {
    @Rule
    public ActivityTestRule<HomeActivity> activityRule =
            new ActivityTestRule<>(HomeActivity.class, true, false);
    private FirebaseFirestore db;
    private String testUsername;
    private String eventId1;
    private String eventId2;
    private String eventIdOther;
    @Before
    public void setUp() throws InterruptedException
    {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth =FirebaseAuth.getInstance();
        final CountDownLatch authLatch=new CountDownLatch(1);
        auth.signInAnonymously().addOnCompleteListener(task -> authLatch.countDown());
        authLatch.await();
        testUsername= "TestUser_"+System.currentTimeMillis();
        User.getInstance().setUsername(testUsername);
        createTestEvent(testUsername,1672537200000L, "Event 2");
        createTestEvent(testUsername, 1672530000000L,"Event 1");
        createTestEvent("OtherUser",  1672526400000L,"Event 3");
        activityRule.launchActivity(new Intent());
        SystemClock.sleep(5000);
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(3000);}
    /**
     * Helper method to create a test Event in Firestore.
     * @param postUser The user who “owns” this event.
     * @param timestamp A long (milliseconds) for the date/time.
     * @param title Title of the mood event.
     */
    private void createTestEvent(String postUser, long timestamp, String title)throws InterruptedException
    {final CountDownLatch latch =new CountDownLatch(1);

        Event event =new Event();
        String docId =  UUID.randomUUID().toString();
        event.setID(docId);
        event.setTitle(title);
        event.setDate(new SimpleDateFormat("dd MMM yyyy, HH:mm",Locale.getDefault()).format(new Date(timestamp)));
        event.setOverlayColor("#FFFFFF");
        event.setImageUrl("");
        event.setEmojiResource(0);
        event.setFollowed(false);
        event.setMyPost(true);
        event.setMood("TestMood");
        event.setMoodExplanation("Testing");
        event.setSituation("Alone");
        event.setPostUser(postUser);
        event.setPublic_status(true);
        Database.getInstance().saveEventToFirebase(event, success -> latch.countDown());
        latch.await();
        if ("OtherUser".equals(postUser))
        {eventIdOther = docId;
        }
        else if (eventId1 == null) {eventId1 = docId;}
        else {eventId2 = docId;}
    }

    @Test
    public void testMoodHistorySorting()
    {
        onView(withId(R.id.recycler_view)).check(new RecyclerViewItemCountAssertion(2));
        onView(RecyclerViewMatcher.withRecyclerView(R.id.recycler_view).atPositionOnView(0, R.id.event_title)).check(matches(withText("Event 2")));
        onView(RecyclerViewMatcher.withRecyclerView(R.id.recycler_view).atPositionOnView(1, R.id.event_title)).check(matches(withText("Event 1")));}
    @After
    public void tearDown() throws InterruptedException
    {   deleteEvent(eventId1);
        deleteEvent(eventId2);
        deleteEvent(eventIdOther);
    }

    private void deleteEvent(String docId) throws InterruptedException
    {
        if (docId == null) return;
        final CountDownLatch latch = new CountDownLatch(1);
        db.collection("Event").document(docId).delete().addOnCompleteListener(task -> latch.countDown());
        latch.await();}

    public static class RecyclerViewMatcher
    {   private final int recyclerViewId;
        public RecyclerViewMatcher(int recyclerViewId) {this.recyclerViewId = recyclerViewId;}
        public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {return new RecyclerViewMatcher(recyclerViewId);}
        public org.hamcrest.Matcher<View> atPositionOnView(final int position, final int targetViewId) {return new org.hamcrest.TypeSafeMatcher<View>() {
                View childView;
                @Override
                public void describeTo(org.hamcrest.Description description) {description.appendText("has view id " + targetViewId + " at position " + position);}
                @Override
                protected boolean matchesSafely(View view)
                {
                    if (childView ==null)
                    {
                        androidx.recyclerview.widget.RecyclerView recyclerView =view.getRootView().findViewById(recyclerViewId);
                        if (recyclerView != null && recyclerView.getId() == recyclerViewId)
                        {   androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder =recyclerView.findViewHolderForAdapterPosition(position);
                            if (viewHolder == null)
                            {return false;}
                            childView = viewHolder.itemView.findViewById(targetViewId);
                        } else {return false;}}
                    return view == childView;}};
        }
    }
    public class RecyclerViewItemCountAssertion implements androidx.test.espresso.ViewAssertion
    {   private final int expectedCount;
        public RecyclerViewItemCountAssertion(int expectedCount) {this.expectedCount = expectedCount;}
        @Override
        public void check(View view, androidx.test.espresso.NoMatchingViewException noViewFoundException)
        {   if (noViewFoundException != null) {throw noViewFoundException;}
            androidx.recyclerview.widget.RecyclerView recyclerView =(androidx.recyclerview.widget.RecyclerView) view;
            androidx.recyclerview.widget.RecyclerView.Adapter adapter=recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(expectedCount));
        }}
}
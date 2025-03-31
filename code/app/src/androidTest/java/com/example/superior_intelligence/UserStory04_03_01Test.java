package com.example.superior_intelligence;
import static androidx.test.espresso.Espresso.onView;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import android.os.SystemClock;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import static androidx.test.espresso.action.ViewActions.click;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.text.SimpleDateFormat;
import java.util.UUID;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import org.junit.After;
import org.junit.Before;
import java.util.concurrent.CountDownLatch;
import java.util.Locale;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import android.os.SystemClock;
import android.util.Log;

/**
 * UI test to see the "Filter By Emotional State" user story (User Story 04.03.01 and issue number 5 including all sub issues).
 * signin anonymously and then makes two mood events for the test user (one with mood "Anger" and one with mood "Happy"), then launches HomeActivity
 * goes to "MYPOSTS" then opens the filter by mood and selects "Anger" mood to apply the filter
 * checks to see that only the "Anger Post" is visible.
 */
@RunWith(AndroidJUnit4.class)
public class UserStory04_03_01Test
{   @Rule
    public ActivityTestRule<HomeActivity> activityRule=new ActivityTestRule<>(HomeActivity.class, true, false);
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String angerEventId;
    private String happyEventId;
    @BeforeClass
    public static void setupEmulator(){FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);}

    @Before
    public void setUp()throws InterruptedException
    {   db= FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();
        final CountDownLatch authLatch= new CountDownLatch(1);
        auth.signInAnonymously().addOnCompleteListener(task -> authLatch.countDown());
        authLatch.await();
        String testUsername ="TestUser_"+System.currentTimeMillis();
        User.getInstance().setUsername(testUsername);
        createTestEvent(testUsername,"Anger Post","Anger","Anger Post");
        createTestEvent(testUsername,"Happy Post", "Happy", "Happy Post");
        activityRule.launchActivity(null);
        SystemClock.sleep(5000);
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(3000);}
    /**
     * Helper method to create a test mood event.
     * @param postUser the username of the event owner
     * @param idPrefix a prefix to help generate a unique ID for the event
     * @param mood the emotional state of the event (e.g. "Anger" or "Happy")
     * @param title the title to display for the event
     */
    private void createTestEvent(String postUser, String idPrefix, String mood, String title) throws InterruptedException
    {   final CountDownLatch latch =new CountDownLatch(1);
        Event event =new Event();
        String docId=idPrefix + "_" + UUID.randomUUID().toString();
        event.setID(docId);
        event.setTitle(title);
        String currentDate=new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        event.setDate(currentDate);
        event.setOverlayColor("#FFFFFF");
        event.setImageUrl("");
        event.setEmojiResource(0);
        event.setFollowed(false);
        event.setMyPost(true);
        event.setMood(mood);
        event.setMoodExplanation("Testing " + mood);
        event.setSituation("Alone");
        event.setPostUser(postUser);
        event.setPublic_status(true);
        Database.getInstance().saveEventToFirebase(event, success -> latch.countDown());
        latch.await();
        if("Anger".equals(mood)) {angerEventId = docId;} else if ("Happy".equals(mood)) {happyEventId = docId;}
    }
    @Test
    public void testFilterByEmotionalState()throws InterruptedException
    {   onView(withId(R.id.menu_button)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.emotional_state_option)).perform(click());
        SystemClock.sleep(2000);
        onView(withText("Anger")).perform(click());
        SystemClock.sleep(1000);
        onView(withText("FILTER")).perform(click());
        SystemClock.sleep(3000);
        onView(withText("Anger Post")).check(matches(isDisplayed()));
        onView(withText("Happy Post")).check(doesNotExist());}
    @After
    public void tearDown(){
        String projectId ="moodgram";
        URL url =null;
        try {url=new URL("http://10.0.2.2:8080/emulator/v1/projects/" +projectId+"/databases/(default)/documents");}
        catch (MalformedURLException exception) {Log.e("URL Error",exception.getMessage());}
        HttpURLConnection urlConnection =null;
        try{urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code","Response Code: "+ response);}
        catch (IOException exception){Log.e("IO Error",exception.getMessage());}
        finally {if(urlConnection !=null) {urlConnection.disconnect();}}
    }
}
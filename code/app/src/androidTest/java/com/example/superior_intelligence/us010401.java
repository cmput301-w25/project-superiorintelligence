package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Test US01.04.01: check if user can interact with an event and view the information
 */
public class us010401 {
    // START AT HOMEPAGE

    @Rule
    public ActivityScenarioRule<HomePageActivity> scenario = new
            ActivityScenarioRule<>(HomePageActivity.class);


    // CREATE MOOD BEFORE TEST
    @Before
    public void addDummyEvent(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodRef = db.collection("MyPosts");
        Mood[] moods = {
            new Mood("Test1", "Anger", "Test test1", "Alone"),
            new Mood("Test2", "Sadness", "Test test2", "Alone")
        };

        for (Mood mood : moods){
            moodRef.document().set(mood);
        }
    }

    @Test
    public void editMoodShouldShowAllInfo() throws InterruptedException {

        //click on MyPosts tab
        onView(withId(R.id.tab_myposts)).perform(click());
        wait(5000);

        //check if dummy data is displayed
        onView(withText("Test1")).check(matches(isDisplayed()));

/*
        // Click on mood event to view info
        onView(withText("Test1")).perform(click());
        wait(1000);

        onView(withText("Mood")).check(matches(withText("Anger")));
        onView(withText("Reason")).check(matches(withText("Test test1")));
        onView(withText("Social Situation")).check(matches(withText("Alone")));*/
    }

    // CLEAR DATABASE AFTER EVERY TEST
    @After
    public void tearDown() {
        String projectId = "moodgram";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    // only run once
    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }
}

package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withInputType;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.util.regex.Pattern.matches;

import android.util.Log;

import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@RunWith(JUnit4.class)
public class us040401 {

    // Start at HomeActivity
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Filter my posts by text should show post when the phrase entered matches mood reason
     * @throws InterruptedException after keyword is enter, wait for firebase to load event(s)
     */
    @Test
    public void filterMyPostsByTextShouldShowPostWithMatchedPhrase() throws InterruptedException {
        // click filter button and choose filter by text to enter keyword
        enterKeyword("text hello");
        // check that the post with matching mood reason is displayed
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));
        // check that post without the matching mood reason is not displayed
        onView(withText("Test during recent week")).check(doesNotExist());
    }

    /**
     * Filter my posts by text should show posts when a word entered is contained in the mood reason
     * @throws InterruptedException after keyword is enter, wait for firebase to load event(s)
     */
    @Test
    public void filterMyPostsByTextShouldShowPostThatContainWord() throws InterruptedException {
        // click filter button and choose filter by text to enter keyword
        enterKeyword("text");
        // check that the post with matching mood reason is displayed
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));
        // check that post without the matching mood reason is not displayed
        onView(withText("Test during recent week")).check(doesNotExist());
    }

    /**
     * Filter my posts by text should show posts when partial word entered is contained in the mood reason
     * @throws InterruptedException after keyword is enter, wait for firebase to load event(s)
     */
    @Test
    public void filterMyPostsByTextShouldShowPostThatContainPartialWord() throws InterruptedException {
        // click filter button and choose filter by text to enter keyword
        enterKeyword("hell");
        // check that the post with matching mood reason is displayed
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Filter my posts by text should show all posts when empty keyword is inputted
     * @throws InterruptedException after keyword is enter, wait for firebase to load event(s)
     */
    @Test
    public void filterMyPostsByTextShouldShowAllPostWithEmptyText() throws InterruptedException {
        // click filter button and choose filter by text to enter keyword
        enterKeyword("");
        // check that the post with matching mood reason is displayed
        onView(withText("Test last year")).check(ViewAssertions.matches(isDisplayed()));
        onView(withText("Test during recent week")).check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Initialize filtering mood event by entering text to find-
     * mood event(s) that contains specified text in the mood reason
     * @param keyword   keyword text/phrase that will be entered
     * @throws InterruptedException after keyword is enter, wait for firebase to load event(s)
     */
    public void enterKeyword(String keyword) throws InterruptedException {
        onView(withId(R.id.tab_myposts)).perform(click());
        onView(withId(R.id.menu_button)).perform(click());
        onView(withId(R.id.text_filter_option)).perform(click());
        onView(withId(R.id.dialog_filter_edit_text)).perform(typeText(keyword));
        onView(withText("FILTER")).perform(click());
        Thread.sleep(5000);
    }

    /**
     * Add events that needed in the testing to the database
     * Log in to start the test
     * @throws InterruptedException wait for posts and user to be added to firebase successfully
     */
    @Before // run before every test
    public void setUp() throws InterruptedException {

        seedMyPost();
        seedUserDB("testUser", "Test User", "TestPass");
        logIn();

    }

    /**
     * Log in to the homepage using testUser3
     * @throws InterruptedException wait for user and post data to load when logging in
     */
    public void logIn() throws InterruptedException {
        onView(withId(R.id.login_button_login_page)).perform(click());

        onView(withId(R.id.login_username)).perform(typeText("testUser")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("TestPass")).perform(closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(5000);
    }

    /**
     * Add user to database with name, user, password
     * @param username      username of the test user
     * @param name          name of the test user
     * @param rawPassword   raw password to be hashed and saved to firebase
     * @throws InterruptedException wait until user is added to firebase successfully
     */
    private void seedUserDB(String username, String name, String rawPassword) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new java.util.ArrayList<>());
        user.put("following", new java.util.ArrayList<>());

        String hashedPassword = PasswordHasher.hashPassword(rawPassword);
        user.put("password", hashedPassword);

        db.collection("users").document(username).set(user)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await();
    }

    /**
     * Add my posts to database with one containing "text" in mood explanation
     * and the other contains no mood explanation
     * @throws InterruptedException wait until event is added to firebase successfully
     */
    public void seedMyPost() throws InterruptedException {
        Event event = new Event("TestBeforeRecWk", "Test last year", "12 MAR 2024, 12:04", "#CC0099", "", 0, false, true, "Surprise", "text hello", "", "testUser", null, null, true);
        Event event2 = new Event("TestDuringRecWk", "Test during recent week", null, "#FF6347", "", 0, false, true, "Anger", "shell on the beach", "", "testUser", null, null, true);

        // current date to ensure test will work regardless of the time
        String eventDate = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date());
        event2.setDate(eventDate);

        seedPostDB(event);
        seedPostDB(event2);
    }

    /**
     * Add an event to database
     * @param event Event to be added to firebase
     * @throws InterruptedException wait until event is added to firebase successfully
     */
    public void seedPostDB(Event event) throws InterruptedException {
        Database db = new Database();
        CollectionReference postsRef = db.getMyPostsRef();
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        postsRef.document(event.getID()).set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event successfully added");
                    latch.countDown(); // Release the lock
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to add event", e);
                    latch.countDown(); // Release the lock even on failure
                });
        latch.await();
    }

    /**
     * Remove any of the data that were added in the beginning of the test
     */
    @After // run after every test
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

    /**
     * Set up emulators before the test class is run
     */
    // only run once
    @BeforeClass
    public static void setupEmulator(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

}

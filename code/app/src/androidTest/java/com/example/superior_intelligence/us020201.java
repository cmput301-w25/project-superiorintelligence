package com.example.superior_intelligence;


import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.intent.matcher.IntentMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class us020201 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);
        ensureUserExists("testUser", "Test User");
        Intents.init();
    }

    @Test
    public void testAddPhotoFlow() throws InterruptedException {
        loginAs("testUser");

        // Start creating event
        onView(withId(R.id.addButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.mood_event_title)).perform(typeText("Photo Mood"));
        closeSoftKeyboard();

        // Click to add photo
        onView(withId(R.id.add_photo_button)).perform(click());
        intended(hasComponent(PhotoActivity.class.getName()));

        // Mock photo selection
        Uri mockPhoto = createSolidColorImage(ApplicationProvider.getApplicationContext());
        Intent resultData = new Intent();
        resultData.setData(mockPhoto);
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        // Upload and confirm
        onView(withId(R.id.upload_photo_button)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.confirm_button)).perform(click());
        SystemClock.sleep(2000);

        // Select emotion
        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(is("Happiness")).perform(click());
        onView(withId(R.id.selected_mood)).check(matches(withText("Happiness")));

        // Enter trigger text
        onView(withId(R.id.trigger_response)).perform(replaceText("With photo!"));
        closeSoftKeyboard();

        // Post the event
        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.public_checkbox)).perform(click());
        onView(withText("POST")).perform(click());

        // Verify in MyPosts
        SystemClock.sleep(3000);
        onView(withId(R.id.tab_myposts)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("Photo Mood")).check(matches(isDisplayed()));
    }

    private void loginAs(String username) throws InterruptedException {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);
        onView(withId(R.id.login_username)).perform(typeText(username));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000);
    }

    private void ensureUserExists(String username, String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("name", name);
        user.put("followers", new ArrayList<>());
        user.put("following", new ArrayList<>());
        db.collection("users").document(username).set(user)
                .addOnCompleteListener(task -> latch.countDown());
        latch.await();
    }

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

    private Uri createSolidColorImage(Context context) {
        Bitmap bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.BLUE);
        File file = new File(context.getCacheDir(), "mock_photo.jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileProvider.getUriForFile(
                context,
                "com.example.superior_intelligence.file_provider",
                file
        );
    }
}

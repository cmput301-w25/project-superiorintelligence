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
import static androidx.test.espresso.Espresso.closeSoftKeyboard;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
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

/**
 * Tests that uploading a photo larger than 64 KB triggers
 * the "size limit exceeded" dialog in PhotoActivity.
 */
@RunWith(AndroidJUnit4.class)
public class us020301 {

    @Rule
    public ActivityTestRule<LoginPageActivity> loginRule =
            new ActivityTestRule<>(LoginPageActivity.class, true, false);

    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Sets up Firebase emulator and ensures test user exists before each test.
     */
    @Before
    public void setup() throws InterruptedException {
        FirebaseAuth.getInstance().signOut();
        db.useEmulator("10.0.2.2", 8080);
        ensureUserExists("testUser", "Test User", "TestPass");
        Intents.init();
    }

    /**
     * Simulates when a user tries to upload a photo larger than 64KB.
     * Verifies that the "Image size exceed limit" dialog is shown with the correct message.
     */
    @Test
    public void testUploadTooLargeImage_ShowsSizeLimitDialog() throws InterruptedException {
        loginAs("testUser","TestPass");

        // Start creating event
        onView(withId(R.id.addButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.mood_event_title)).perform(typeText("Large Photo Test"));
        closeSoftKeyboard();

        // Click to add photo
        onView(withId(R.id.add_photo_button)).perform(click());
        intended(hasComponent(PhotoActivity.class.getName()));

        Uri largeImageUri = createLargeMockImageUri(ApplicationProvider.getApplicationContext());
        Intent resultData = new Intent();
        resultData.setData(largeImageUri);
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        onView(withId(R.id.upload_photo_button)).perform(click());

        onView(withId(R.id.size_exceeded_title))
                .check(matches(withText("Image size exceed limit")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.error_message))
                .check(matches(withText("Image must be under 65536 bytes")))
                .check(matches(isDisplayed()));

        onView(withText(R.string.ok_button_text)).perform(click());
    }

    /**
     * Creates a large bitmap, writes it at 100% JPEG quality,
     * and returns a content:// Uri from FileProvider.
     * This ensures the final size easily exceeds 64KB even after re-compression in PhotoActivity.
     */
    private Uri createLargeMockImageUri(Context context) {
        // something large like 4096x4096
        int width = 4096;
        int height = 4096;
        Bitmap bigBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // fill every pixel with random colors (make the JPEG larger)
        Random random = new Random();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                bigBitmap.setPixel(x, y, color);
            }
        }

        File cacheFile = new File(context.getCacheDir(), "large_test_image.jpg");
        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            bigBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(
                context,
                "com.example.superior_intelligence.file_provider",
                cacheFile
        );
    }

    /**
     * Logs in using the given username.
     */
    private void loginAs(String username, String password) throws InterruptedException {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginRule.launchActivity(intent);

        onView(withId(R.id.login_username)).perform(typeText(username));
        ViewActions.closeSoftKeyboard();

        onView(withId(R.id.login_password)).perform(typeText(password));
        ViewActions.closeSoftKeyboard();

        onView(withId(R.id.login_button)).perform(click());
        SystemClock.sleep(3000);
    }

    /**
     * Creates a test user in the Firestore emulator if not already present.
     */
    private void ensureUserExists(String username, String name, String rawPassword) throws InterruptedException {
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
     * Clears all documents from Firestore after tests.
     */
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
}

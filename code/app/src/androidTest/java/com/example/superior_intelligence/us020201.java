package com.example.superior_intelligence;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class us020201 {

    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES);


    @Rule
    public IntentsTestRule<MoodCreateAndEditActivity> moodCreateRule =
            new IntentsTestRule<>(MoodCreateAndEditActivity.class, true, true);
    /**
     * Tests the process of adding a photo, selecting an emotion, providing a reason,
     * and confirming a mood entry, ensuring navigation between activities works correctly.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void testAddPhotoFlow() throws InterruptedException {

        onView(withId(R.id.mood_event_title)).check(matches(isDisplayed()));


        onView(withId(R.id.add_photo_button)).perform(click());
        intended(hasComponent(PhotoActivity.class.getName()));


        Uri colorImageUri = createSolidColorImage(moodCreateRule.getActivity());
        Intent resultData = new Intent();
        resultData.setData(colorImageUri);
        Instrumentation.ActivityResult stubResult =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);


        intending(hasAction(Intent.ACTION_PICK)).respondWith(stubResult);


        onView(withId(R.id.upload_photo_button)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.confirm_button)).perform(click());
        onView(withId(R.id.emotion_arrow)).check(matches(isDisplayed()));

        onView(withId(R.id.emotion_arrow)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Happiness"))).perform(click());
        onView(withId(R.id.trigger_response)).perform(typeText("Reason for mood"));

        onView(withId(R.id.confirm_mood_create_button)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    /**
     * Creates a 512x512 single-color (BLUE) bitmap and returns a FileProvider Uri.
     */
    private Uri createSolidColorImage(Context context) {
        int width = 512;
        int height = 512;
        Bitmap solidBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        solidBitmap.eraseColor(Color.BLUE); // Entire bitmap is solid BLUE

        File file = new File(context.getCacheDir(), "solid_test_image.jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            solidBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            fos.flush();
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

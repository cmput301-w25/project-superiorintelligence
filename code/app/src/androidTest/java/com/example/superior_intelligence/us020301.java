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
import java.util.Random;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.Intents.intending;

/**
 * Tests that uploading a photo larger than 64 KB triggers
 * the "size limit exceeded" dialog in PhotoActivity.
 */
@RunWith(AndroidJUnit4.class)
public class us020301 {

    // automatically grant the gallery permission.
    @Rule
    public GrantPermissionRule grantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_MEDIA_IMAGES);

    @Rule
    public IntentsTestRule<PhotoActivity> photoActivityRule =
            new IntentsTestRule<>(PhotoActivity.class, true, true);

    @Test
    public void testUploadTooLargeImage_ShowsSizeLimitDialog() throws InterruptedException {
        Uri largeImageUri = createLargeMockImageUri(photoActivityRule.getActivity());
        Intent resultData = new Intent();
        resultData.setData(largeImageUri);

        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Whenever PhotoActivity calls ACTION_PICK, respond with our big image result.
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);


        onView(withId(R.id.upload_photo_button)).perform(click());

        // wait briefly for PhotoActivity to process
        Thread.sleep(2000);

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
}

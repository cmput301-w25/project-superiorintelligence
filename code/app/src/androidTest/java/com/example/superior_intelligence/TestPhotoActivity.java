package com.example.superior_intelligence;
import static org.junit.Assert.*;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class) // Ensure Android JUnit Runner is used
public class TestPhotoActivity {

    private ActivityScenario<PhotoActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(PhotoActivity.class);
    }

    /*** Test if PhotoActivity instance is created ***/
    @Test
    public void testActivityCreation() {
        scenario.onActivity(Assert::assertNotNull);

    }

    /*** Test Creating Image File ***/
    @Test
    public void testCreateImageFile() {
        scenario.onActivity(activity -> {
            File file = activity.createImageFile();
            assertNotNull(file);
            assertTrue(file.getAbsolutePath().contains("JPEG_"));
        });
    }

    /*** Test Image Compression ***/
    @Test
    public void testGetCompressedBitmap() {
        scenario.onActivity(activity -> {
            Uri imageUri = Uri.parse("content://com.example.superior_intelligence/test.jpg");
            Bitmap bitmap = activity.getCompressedBitmap(imageUri);
            assertNull(bitmap);
        });
    }

    /*** Test Saving Compressed Image ***/
    @Test
    public void testSaveCompressedImage() {
        scenario.onActivity(activity -> {
            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

            try {
                // Access the private method
                Method method = PhotoActivity.class.getDeclaredMethod("saveCompressedImage", Bitmap.class);
                method.setAccessible(true); // Bypass private access

                method.invoke(activity, bitmap);  // Call the method

                assertNotNull(bitmap);
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            }
        });
    }

    /*** Test Showing Size Exceeded Dialog ***/
    @Test
    public void testShowSizeExceededDialog() {
        scenario.onActivity(activity -> {
            try {
                activity.showSizeExceededDialog();
                assertTrue(true);
            } catch (Exception e) {
                fail("Dialog crashed: " + e.getMessage());
            }
        });
    }
}
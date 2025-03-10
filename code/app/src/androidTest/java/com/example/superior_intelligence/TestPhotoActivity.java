package com.example.superior_intelligence;
import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

@RunWith(AndroidJUnit4.class) // Ensure Android JUnit Runner is used
public class TestPhotoActivity {

    private ActivityScenario<PhotoActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(PhotoActivity.class);
    }

    /**
     * Tests if PhotoActivity is successfully created.
     */
    @Test
    public void testActivityCreation() {
        scenario.onActivity(Assert::assertNotNull);

    }
    /**
     * Tests if a valid image URI is correctly converted to a Bitmap.
     */
    @Test
    public void testUriToBitmap() throws Exception {
        scenario.onActivity(activity -> {
            Uri validUri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + R.drawable.testing_image);

            try {
                // Access private method
                Method method = PhotoActivity.class.getDeclaredMethod("uriToBitmap", Uri.class);
                method.setAccessible(true);

                Bitmap bitmap = (Bitmap) method.invoke(activity, validUri);
                assertNotNull("Bitmap should not be null", bitmap);
            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        });
    }

    /**
     * Tests converting a Bitmap to a Base64 string.
     */
    @Test
    public void testToBase64() {
        scenario.onActivity(activity -> {
            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            String base64String = activity.toBase64(bitmap);
            assertNotNull(base64String);
            assertFalse(base64String.isEmpty());
        });
    }
    /**
     * Tests uploading an image and verifies a valid document ID is returned.
     */
    @Test
    public void testUploadImage() {
        scenario.onActivity(activity -> {
            Uri fakeUri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + R.drawable.testing_image);

            PhotoActivity.UploadCallback callback = new PhotoActivity.UploadCallback() {
                @Override
                public void onUploadComplete(String documentID) {
                    assertNotNull("Document ID should not be null", documentID);
                    assertFalse("Document ID should not be empty", documentID.isEmpty());
                }
            };

            activity.uploadImage(fakeUri, callback, "testUser");
        });
    }

    /**
     * Tests if an image file is successfully created.
     */
    @Test
    public void testCreateImageFile() {
        scenario.onActivity(activity -> {
            File file = activity.createImageFile();
            assertNotNull(file);
            assertTrue(file.getAbsolutePath().contains("JPEG_"));
        });
    }
    /**
     * Tests if an image exceeding the size limit triggers the warning dialog.
     */
    @Test
    public void testImageSizeLimit() {
        scenario.onActivity(activity -> {
            try {
                // Generate a large image
                Bitmap largeBitmap = Bitmap.createBitmap(4000, 4000, Bitmap.Config.ARGB_8888);

                // Save the bitmap as a temporary file and get its URI
                File tempFile = new File(activity.getCacheDir(), "temp_large_image.jpg");
                FileOutputStream fos = new FileOutputStream(tempFile);
                largeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                Uri tempUri = Uri.fromFile(tempFile);

                // Log image size
                int imageSize = (int) tempFile.length();
                Log.d("TestPhotoActivity", "Generated image file size: " + imageSize + " bytes");

                // Ensure image is actually large
                assertTrue("Generated image should be larger than 65536 bytes, but was: " + imageSize, imageSize > 65536);

                // Call processSelectedImage() using reflection
                Method method = PhotoActivity.class.getDeclaredMethod("processSelectedImage", Uri.class);
                method.setAccessible(true);
                method.invoke(activity, tempUri);

                // Wait for the dialog to appear (UI update delay)
                Thread.sleep(1000); // Give UI time to display the dialog

                // Validate that the dialog is displayed
                assertNotNull("Warning dialog should be displayed", activity.sizeLimitDialog);
                assertTrue("Dialog should be showing", activity.sizeLimitDialog.isShowing());

            } catch (Exception e) {
                fail("Reflection failed: " + e.getMessage());
            }
        });
    }

}
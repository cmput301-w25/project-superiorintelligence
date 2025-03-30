package com.example.superior_intelligence;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class TestPhotos {

    private Context mockContext;
    private Photobase photobase;

    /**
     * Sets up a spied Photobase instance with a mocked context.
     * Also mocks out Base64 encoding so tests don’t hit Android internals.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockContext = mock(Context.class);
        photobase = spy(new Photobase(mockContext, null)); // Firestore not used in tests

        // Override Base64 encoding for test environment
        doAnswer(invocation -> {
            byte[] input = invocation.getArgument(0);
            return Base64.getEncoder().encodeToString(input);
        }).when(photobase).encodeToBase64(any());
    }

    /**
     * Makes sure uploadImage() fails gracefully if the compressed image
     * is too big (over 1MB), and calls the failure callback.
     */
    @Test
    public void testUploadTooLargeImage() throws IOException {
        Uri mockUri = mock(Uri.class);
        Bitmap mockBitmap = mock(Bitmap.class);
        doReturn(mockBitmap).when(photobase).uriToBitmap(any());

        // Simulate image > 1MB
        doAnswer(invocation -> {
            ByteArrayOutputStream stream = (ByteArrayOutputStream) invocation.getArgument(2);
            stream.write(new byte[1048577]);
            return true;
        }).when(mockBitmap).compress(any(), anyInt(), any());

        Photobase.UploadCallback mockCallback = mock(Photobase.UploadCallback.class);
        photobase.uploadImage(mockUri, "testUser", mockCallback);

        verify(mockCallback).onUploadFailed("Image size too large");
    }

    /**
     * Tests what happens when uriToBitmap() returns null —
     * the upload should fail and call the proper callback.
     */
    @Test
    public void testUploadFailsWhenBitmapIsNull() {
        Uri mockUri = mock(Uri.class);
        doReturn(null).when(photobase).uriToBitmap(any());

        Photobase.UploadCallback mockCallback = mock(Photobase.UploadCallback.class);
        photobase.uploadImage(mockUri, "testUser", mockCallback);

        verify(mockCallback).onUploadFailed("Bitmap conversion failed");
    }

    /**
     * Checks that uriToBitmap() doesn’t crash the app if something goes wrong
     * when trying to open the image — should just return null.
     */
    @Test
    public void testUriToBitmapHandlesException() {
        Uri mockUri = mock(Uri.class);
        Context context = mock(Context.class);

        // Simulate a crash when trying to open the input stream
        when(context.getContentResolver()).thenThrow(new RuntimeException("bad stream"));

        Photobase testPhotobase = new Photobase(context, null);
        Bitmap result = testPhotobase.uriToBitmap(mockUri);

        assertNull(result);
    }

    /**
     * Makes sure base64ToBitmap() returns null if the string
     * isn’t actually valid Base64 — helps catch bad input early.
     */
    @Test
    public void testBase64ToBitmapReturnsNullOnBadInput() {
        // Invalid Base64 string
        String badBase64 = "not-real-base64";

        // We need access to the real method since it's private
        Photobase realPhotobase = new Photobase(mockContext, null);
        Bitmap result = invokeBase64ToBitmap(realPhotobase, badBase64);

        assertNull(result);
    }

    /**
     * Uses reflection to call the private base64ToBitmap() method
     * so we can test it without changing the original class.
     */
    private Bitmap invokeBase64ToBitmap(Photobase instance, String base64) {
        try {
            var method = Photobase.class.getDeclaredMethod("base64ToBitmap", String.class);
            method.setAccessible(true);
            return (Bitmap) method.invoke(instance, base64);
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed", e);
        }
    }

    /**
     * Verifies that encodeToBase64() gives the same output as
     * Java’s Base64 encoder — just a sanity check.
     */
    @Test
    public void testEncodeToBase64ReturnsExpectedString() {
        Photobase realPhotobase = new Photobase(mockContext, null);
        byte[] data = "hello".getBytes();

        String encoded = realPhotobase.encodeToBase64(data);

        // Should match standard Base64 encoding
        String expected = Base64.getEncoder().encodeToString(data);
        org.junit.Assert.assertEquals(expected, encoded);
    }
}

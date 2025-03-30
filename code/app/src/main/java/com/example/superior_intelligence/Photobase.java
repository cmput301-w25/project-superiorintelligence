package com.example.superior_intelligence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Photobase {
    private static Photobase instance;
    private final FirebaseFirestore db;
    private final Context context;

    public Photobase(@NonNull Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    public Photobase(@NonNull Context context, @NonNull FirebaseFirestore firestore) {
        this.db = firestore;
        this.context = context;
    }

    public static void setInstanceForTesting(FirebaseFirestore firestore, Context context) {
        instance = new Photobase(context, firestore); // Optional singleton pattern
    }

    // Callback interface for loading images
    public interface ImageLoadCallback {
        void onImageLoaded(Bitmap bitmap);
        void onImageLoadFailed(String error);
    }

    // Callback interface for uploading images
    public interface UploadCallback {
        void onUploadComplete(String documentID);
        void onUploadFailed(String error);
    }

    /**
     * Loads an image from Firestore using a document ID.
     * @param documentId The Firestore document ID.
     * @param callback   Callback for image loading.
     */
    public void loadImage(String documentId, ImageLoadCallback callback) {
        db.collection("images").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String base64Image = documentSnapshot.getString("imgData");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = base64ToBitmap(base64Image);
                            if (bitmap != null) {
                                callback.onImageLoaded(bitmap);
                            } else {
                                callback.onImageLoadFailed("Bitmap conversion failed");
                            }
                        } else {
                            callback.onImageLoadFailed("No image data found");
                        }
                    } else {
                        callback.onImageLoadFailed("Document does not exist");
                    }
                })
                .addOnFailureListener(e -> callback.onImageLoadFailed("Failed to retrieve image: " + e.getMessage()));
    }

    /**
     * Uploads an image to Firestore.
     * @param imageUri The URI of the image to upload.
     * @param uid      The user ID to associate with the image.
     * @param callback Callback for upload completion.
     */
    public void uploadImage(Uri imageUri, String uid, UploadCallback callback) {
        Bitmap bitmap = uriToBitmap(imageUri);
        if (bitmap == null) {
            callback.onUploadFailed("Bitmap conversion failed");
            return;
        }
        // Compress and convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        byte[] byteArray = stream.toByteArray();
        // Check size (here, we ensure it's not over 1MB)
        if (byteArray.length > 1048576) {
            callback.onUploadFailed("Image size too large");
            return;
        }
        //String convertedImg = Base64.encodeToString(byteArray, Base64.DEFAULT);
        String convertedImg = encodeToBase64(byteArray);
        // Get current date for record
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        DocumentReference imgDocRef = db.collection("images").document();
        String documentID = imgDocRef.getId();
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imgData", convertedImg);
        imageData.put("imgUser", uid);
        imageData.put("imgDateUpload", currentDate);

        imgDocRef.set(imageData)
                .addOnSuccessListener(aVoid -> callback.onUploadComplete(documentID))
                .addOnFailureListener(e -> callback.onUploadFailed("Upload failed: " + e.getMessage()));
    }


    // Converts byte array to Base64 string (split out for mocking)
    public String encodeToBase64(byte[] byteArray) {
        return java.util.Base64.getEncoder().encodeToString(byteArray);
    }

    /**
     * Converts a Base64 string to a Bitmap.
     * @param base64Str The Base64 string.
     * @return The decoded Bitmap, or null if conversion fails.
     */
    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            //Log.e("ImageRepository", "Error decoding Base64", e);
            System.err.println("Error decoding Base64 " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a given URI into a Bitmap.
     * Requires the application context.
     * @param uri The image URI.
     * @return The Bitmap, or null if conversion fails.
     */
    public Bitmap uriToBitmap(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (IOException | RuntimeException e) {
            //Log.e("ImageRepository", "Error converting URI to bitmap", e);
            System.err.println("Error converting URI to bitmap: " + e.getMessage());
            return null;
        }
    }
}

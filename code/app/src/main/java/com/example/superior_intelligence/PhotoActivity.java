package com.example.superior_intelligence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PhotoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri photoUri;
    private FirebaseFirestore db;
    private String selectedPhotoDocID = null;

    /**
     * Creates the activity, setting up Firestore and button click listeners.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_add);

        db = FirebaseFirestore.getInstance();

        // Back button returns to MoodCreateAndEditActivity
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Confirm button goes back to MoodCreateAndEditActivity
        LinearLayout confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            if (selectedPhotoDocID != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("imageDocID", selectedPhotoDocID);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select an image before confirming.", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView photoIcon = findViewById(R.id.photo_icon);
        photoIcon.setVisibility(View.VISIBLE); // Ensure placeholder icon is visible

        findViewById(R.id.take_photo_button).setOnClickListener(view -> checkCameraPermission());
        findViewById(R.id.upload_photo_button).setOnClickListener(view -> checkGalleryPermission());
    }

    /**
     * Checks if the app has camera permission and requests it if not.
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Checks if the app has gallery access permission and requests it if not.
     */
    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openGallery();
        }
    }

    /**
     * Handles permission request results.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    openCamera();
                } else {
                    openGallery();
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handles launching the camera actions like comfirming or back
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            processSelectedImage(photoUri);
                        }
                    });

    /**
     * Opens the device's camera and captures an image.
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.superior_intelligence.file_provider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(cameraIntent);
            }
        }
    }

    /**
     * Handles launching the gallery actions like comfirming or back
     */
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImage = result.getData().getData();
                            processSelectedImage(selectedImage);
                        }
                    });

    /**
     * Opens the device's gallery and user picks an image
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    /**
     * Creates a temporary image file in the storage directory.
     * @return The created image file.
     */
    protected File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;

        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e("PhotoActivity", "Error creating file", e);
        }
        return imageFile;
    }
    /**
     * Processes the selected image by checking its size, displaying it if it's small enough,
     * and optionally uploading it to Firestore.
     * @param imageUri The URI of the selected or captured image.
     */
    void processSelectedImage(Uri imageUri) {
        Bitmap bitmap = uriToBitmap(imageUri);
        if (bitmap == null) return;

        // Convert the bitmap to a byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream); // Compress to JPEG format
        byte[] byteArray = stream.toByteArray();
        int imageSize = byteArray.length; // Get the size in bytes
        Log.d("PhotoActivity", "Image size: " + imageSize + " bytes");
        // Check if the image size exceeds 65536 bytes (64KB)
        if (imageSize > 65536) {
            Log.d("PhotoActivity", "Showing size limit exceeded dialog");
            showSizeLimitExceededDialog(); // Show the warning dialog
            return;
        }

        ImageView photoImageView = findViewById(R.id.photo);
        ImageView photoIcon = findViewById(R.id.photo_icon);

        photoImageView.setImageBitmap(bitmap);
        photoImageView.setVisibility(View.VISIBLE);
        photoIcon.setVisibility(View.GONE);

        uploadSelectedImage(imageUri);
    }

    /**
     * Converts a given URI into a Bitmap.
     * @param uri The image URI.
     * @return The converted Bitmap.
     */
    Bitmap uriToBitmap(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            Log.e("PhotoActivity", "Error converting URI to bitmap", e);
            return null;
        }
    }

    /**
     * Uploads the selected image to Firestore.
     * @param imgURI The image URI.
     */
    private void uploadSelectedImage(Uri imgURI) {
        User user = User.getInstance();
        String username = user.getUsername();

        uploadImage(imgURI, documentID -> {
            Log.d("PhotoActivity", "Image uploaded successfully, ID: " + documentID);
            selectedPhotoDocID = documentID;

            // Send document ID back to MoodCreateAndEditActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("imageDocID", documentID);
            setResult(RESULT_OK, resultIntent);

            // Notify user and exit PhotoActivity
            Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show();

        }, username);
    }
    AlertDialog sizeLimitDialog;
    /**
     * Displays a dialog that the selected image exceeded the size limit (64KB).
     * The user can dismiss the dialog by clicking the OK button.
     */
    private void showSizeLimitExceededDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.size_limit_exceeded, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        sizeLimitDialog = builder.create(); // Store the dialog instance
        sizeLimitDialog.show();

        // Find the OK button and set click listener
        Button okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> sizeLimitDialog.dismiss());
    }

    /**
     * Uploads an image to Firestore.
     * @param imgURI The image URI.
     * @param callback The callback to notify upon completion.
     * @param uid The user ID.
     */
    public void uploadImage(Uri imgURI, UploadCallback callback, String uid) {
        String documentID;

        Bitmap bitmap = uriToBitmap(imgURI);
        if (bitmap == null) {
            Log.d("IMG", "Bitmap conversion failed");
            return;
        }

        String convertedImg = toBase64(bitmap);
        if (convertedImg == null || convertedImg.length() > 1048576) {
            Log.d("IMG", "BMP -> B64 conversion failed OR size > 1MB");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(calendar.getTime());

        DocumentReference imgDocRef = db.collection("images").document();
        documentID = imgDocRef.getId();
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imgData", convertedImg);
        imageData.put("imgUser", uid);
        imageData.put("imgDateUpload", currentDate);
        imgDocRef.set(imageData)
                .addOnSuccessListener(aVoid -> callback.onUploadComplete(documentID))
                .addOnFailureListener(e -> Log.d("IMG", "Upload failed"));
    }

    /**
     * Converts a Bitmap to a Base64 string.
     * @param bitmap The image Bitmap.
     * @return The Base64 string representation.
     */
    String toBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Interface for upload completion callback.
     */
    public interface UploadCallback {
        void onUploadComplete(String documentID);
    }
}
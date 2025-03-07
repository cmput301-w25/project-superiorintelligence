package com.example.superior_intelligence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri photoUri;
    private String currentPhotoPath; // Stores the image file path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_add);

        // Back button returns to MoodCreateAndEditActivity
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Confirm button goes back to MoodCreateAndEditActivity
        LinearLayout confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> finish());

        ImageView photoIcon = findViewById(R.id.photo_icon);
        photoIcon.setVisibility(View.VISIBLE); // Ensure placeholder icon is visible

        findViewById(R.id.take_photo_button).setOnClickListener(view -> checkCameraPermission());
        findViewById(R.id.upload_photo_button).setOnClickListener(view -> checkGalleryPermission());
    }

    /***  CAMERA PERMISSION CHECK  ***/
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    /***  GALLERY PERMISSION CHECK  ***/
    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else { // Android 12 and below
            openGallery();
        }
    }

    /***  HANDLE PERMISSION REQUEST RESULT  ***/
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

    /***  CAMERA LAUNCHER  ***/
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            processSelectedImage(photoUri);
                        }
                    });

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

    /***  GALLERY LAUNCHER  ***/
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImage = result.getData().getData();
                            processSelectedImage(selectedImage);
                        }
                    });

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    /***  CREATE A FILE TO STORE IMAGE  ***/
    protected File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;

        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = imageFile.getAbsolutePath(); // Save the file path
        } catch (IOException e) {
            Log.e("PhotoActivity", "Error creating file", e);
        }
        return imageFile;
    }

    /***  PROCESS SELECTED IMAGE  ***/
    private void processSelectedImage(Uri imageUri) {
        Bitmap compressedBitmap = getCompressedBitmap(imageUri);
        if (compressedBitmap == null) return; // Stop if image exceeds size limit

        ImageView photoImageView = findViewById(R.id.photo);
        ImageView photoIcon = findViewById(R.id.photo_icon);

        photoImageView.setImageBitmap(compressedBitmap);
        photoImageView.setVisibility(View.VISIBLE);
        photoIcon.setVisibility(View.GONE);

        saveCompressedImage(compressedBitmap);
    }

    /***  COMPRESS IMAGE & CHECK SIZE LIMIT (64 KB)  ***/
    protected Bitmap getCompressedBitmap(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("PhotoActivity", "InputStream is null");
                return null;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reduce size by half
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap == null) {
                Log.e("PhotoActivity", "Bitmap decoding failed");
                return null;
            }

            // Compress bitmap into a byte array and check size
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
            if (!success) {
                Log.e("PhotoActivity", "Compression failed");
                return null;
            }

            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            if (imageBytes.length > 65536) { // 64 KB limit
                showSizeExceededDialog();
                return null;
            }

            return bitmap;
        } catch (IOException e) {
            Log.e("PhotoActivity", "Error loading image", e);
            return null;
        }
    }

    /***  SHOW SIZE LIMIT EXCEEDED DIALOG  ***/
    protected void showSizeExceededDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.size_limit_exceeded, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> dialog.dismiss());
    }

    /***  SAVE COMPRESSED IMAGE  ***/
    private void saveCompressedImage(Bitmap bitmap) {
        try {
            File compressedImageFile = createImageFile();
            OutputStream outputStream = getContentResolver().openOutputStream(Uri.fromFile(compressedImageFile));

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                outputStream.close();
            } else {
                Log.e("PhotoActivity", "Failed to open output stream");
            }
        } catch (IOException e) {
            Log.e("PhotoActivity", "Error saving image", e);
        }
    }

    /***  GET IMAGE URL FOR ADAPTER  ***/
    public String getImageUrl() {
        return currentPhotoPath;
    }
}

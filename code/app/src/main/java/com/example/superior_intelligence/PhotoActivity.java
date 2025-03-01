/***  SOURCES
 * https://developer.android.com/about/versions/14/changes/partial-photo-video-access
 * https://www.youtube.com/watch?v=D3JCtaK8LSU ***/

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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri photoUri; // Store image file path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_add);

        ImageView photoIcon = findViewById(R.id.photo_icon);
        photoIcon.setVisibility(View.VISIBLE); // Ensure placeholder icon is visible on start

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

    /***  GALLERY PERMISSION CHECK (Handles Android 13+)  ***/
    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else { // Android 12 and below (No need to request permission)
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

    /***  CAMERA LAUNCHER (Saves full-resolution image)  ***/
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            ImageView photoImageView = findViewById(R.id.photo);
                            ImageView photoIcon = findViewById(R.id.photo_icon);

                            // Resize and compress the image before displaying
                            Bitmap compressedBitmap = getCompressedBitmap(photoUri);

                            if (compressedBitmap != null) {
                                photoImageView.setImageBitmap(compressedBitmap);
                                photoImageView.setVisibility(View.VISIBLE);
                                photoIcon.setVisibility(View.GONE);

                                // Save compressed image back to file
                                saveCompressedImage(compressedBitmap);
                            }
                        }
                    });

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure the device has a camera
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
                            if (selectedImage == null) {
                                Log.e("PhotoActivity", "galleryLauncher: selectedImage URI is null");
                                return;
                            }

                            ImageView photoImageView = findViewById(R.id.photo);
                            ImageView photoIcon = findViewById(R.id.photo_icon);

                            Bitmap compressedBitmap = getCompressedBitmap(selectedImage);

                            if (compressedBitmap != null) {
                                photoImageView.setImageBitmap(compressedBitmap);
                                photoImageView.setVisibility(View.VISIBLE);
                                photoIcon.setVisibility(View.GONE);
                            } else {
                                Log.e("PhotoActivity", "galleryLauncher: Failed to load image from gallery.");
                            }
                        }
                    });

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    /***  CREATE A FILE TO STORE THE IMAGE  ***/
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;

        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e("PhotoActivity", "Error occurred", e);
        }
        return imageFile;
    }

    /***  COMPRESS IMAGE TO FIT UNDER 64 KB  ***/
    private Bitmap getCompressedBitmap(Uri imageUri) {
        if (imageUri == null) {
            Log.e("PhotoActivity", "getCompressedBitmap: imageUri is null");
            return null;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("PhotoActivity", "getCompressedBitmap: Failed to open InputStream");
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reduce size by half
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return bitmap;

        } catch (IOException e) {
            Log.e("PhotoActivity", "Error loading bitmap from URI", e);
            return null;
        }
    }

    /***  SAVE COMPRESSED IMAGE TO FILE  ***/
    private void saveCompressedImage(Bitmap bitmap) {
        try {
            File compressedImageFile = createImageFile();
            OutputStream outputStream = getContentResolver().openOutputStream(Uri.fromFile(compressedImageFile));

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                outputStream.close();
                Log.i("PhotoActivity", "Image saved successfully: " + compressedImageFile.getAbsolutePath());
            } else {
                Log.e("PhotoActivity", "Failed to open output stream for saving compressed image.");
            }

        } catch (IOException e) {
            Log.e("PhotoActivity", "Error saving compressed image", e);
        }
    }
}
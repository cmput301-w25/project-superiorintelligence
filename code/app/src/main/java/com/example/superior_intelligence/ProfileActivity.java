package com.example.superior_intelligence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI elements
    private TextView nameTextView, usernameTextView;
    private ImageView profileImageView;
    private ImageView editProfileButton; // single edit button for both name and photo

    // SharedPreferences for local user data
    private SharedPreferences prefs;

    // Firestore reference
    private FirebaseFirestore db;

    // ActivityResultLauncher for picking images
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadProfileImage(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Retrieve stored details
        String name = prefs.getString("name", "Default Name");
        String username = prefs.getString("username", "Default Username");
        String encodedPhoto = prefs.getString("photo", null);

        // Find UI elements
        nameTextView = findViewById(R.id.profile_name);
        usernameTextView = findViewById(R.id.profile_username);
        profileImageView = findViewById(R.id.profile_image_png);
        editProfileButton = findViewById(R.id.profile_edit_button);

        // Set initial values
        nameTextView.setText(name);
        usernameTextView.setText(username);

        if (encodedPhoto != null) {
            loadProfilePhotoFromBase64(encodedPhoto);
        } else {
            // If no photo in SharedPreferences, try to load from Firestore
            loadProfilePhotoFromFirestore(username);
        }

        // Back button
        ImageButton backButton = findViewById(R.id.profile_back_button);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        });

        // Search User button
        LinearLayout searchUserButton = findViewById(R.id.search_user_button);
        searchUserButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SearchUsersActivity.class));
            finish();
        });

        // Sign out button
        LinearLayout signoutButton = findViewById(R.id.signout);
        signoutButton.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(ProfileActivity.this, LoginPageActivity.class));
            finish();
        });

        // Single edit button for both name and profile photo
        editProfileButton.setOnClickListener(v -> showEditOptionsDialog());
    }

    /**
     * Loads profile photo from Firestore if not present in SharedPreferences.
     */
    private void loadProfilePhotoFromFirestore(String username) {
        if (username == null || username.isEmpty()) return;
        db.collection("profile_photo").document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String encodedPhoto = doc.getString("photo");
                        if (encodedPhoto != null && !encodedPhoto.isEmpty()) {
                            prefs.edit().putString("photo", encodedPhoto).apply();
                            loadProfilePhotoFromBase64(encodedPhoto);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load profile photo from Firestore", e));
    }

    /**
     * Displays a dialog offering options to either change name or change profile photo.
     */
    private void showEditOptionsDialog() {
        String[] options = {"Change Name", "Change Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditNameDialog();
            } else if (which == 1) {
                openImagePicker();
            }
        });
        builder.show();
    }

    /**
     * Shows an AlertDialog with an EditText to update the user's full name.
     */
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Full Name");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(nameTextView.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                prefs.edit().putString("name", newName).apply();
                nameTextView.setText(newName);
                updateUserNameInFirestore(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Opens the gallery for image picking.
     */
    private void openImagePicker() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePickerLauncher.launch(pickIntent);
    }

    /**
     * Loads the profile photo from a Base64-encoded string.
     */
    private void loadProfilePhotoFromBase64(String encodedImage) {
        try {
            byte[] imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profileImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode profile image.", e);
        }
    }

    /**
     * Uploads the profile image by compressing and encoding it as Base64,
     * then stores it in the Firestore "profile_photo" collection.
     */
    private void uploadProfileImage(Uri imageUri) {
        String username = prefs.getString("username", null);
        if (username == null) {
            Log.e(TAG, "Username is null. Cannot upload profile image.");
            return;
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            Map<String, Object> photoData = new HashMap<>();
            photoData.put("username", username);
            photoData.put("photo", encodedImage);

            db.collection("profile_photo").document(username)
                    .set(photoData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Profile photo updated in Firestore.");
                        prefs.edit().putString("photo", encodedImage).apply();
                        profileImageView.setImageBitmap(bitmap);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update profile photo in Firestore.", e));
        } catch (IOException e) {
            Log.e(TAG, "Error processing selected image", e);
        }
    }

    /**
     * Updates the user's full name in the Firestore "users" collection.
     */
    private void updateUserNameInFirestore(String newName) {
        String username = prefs.getString("username", null);
        if (username == null) return;
        db.collection("users").document(username)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User name updated in Firestore."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update user name in Firestore.", e));
    }
}

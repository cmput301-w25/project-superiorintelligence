package com.example.superior_intelligence;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a Google Map showing mood event markers retrieved from Firestore.
 * When a marker is tapped, an AlertDialog is shown with all the details of the event.
 * <p>
 * Markers are loaded based on Firestore documents in the "MyPosts" collection, and users
 * can filter the markers using various CheckBoxes (e.g., "Last 12 hours", "Confusion", etc.).
 * </p>
 */
public class MoodMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MoodMap";
    private GoogleMap googleMap;
    private FirebaseFirestore db;

    // Filter CheckBoxes
    private CheckBox cbLast12Hours, cbConfusion, cbAnger, cbFear,
            cbDisgust, cbHappy, cbSad, cbShame, cbSurprise;

    /**
     * Called when the activity is first created. Sets up the UI, finds filter checkboxes,
     * sets up the Apply Filters and Back buttons, and initializes the Google Map fragment.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map); // Must contain <fragment> for SupportMapFragment

        db = FirebaseFirestore.getInstance();

        // Find checkboxes
        cbLast12Hours = findViewById(R.id.cb_last_12_hours);
        cbConfusion   = findViewById(R.id.cb_confusion);
        cbAnger       = findViewById(R.id.cb_anger);
        cbFear        = findViewById(R.id.cb_fear);
        cbDisgust     = findViewById(R.id.cb_disgust);
        cbHappy       = findViewById(R.id.cb_happy);
        cbSad         = findViewById(R.id.cb_sad);
        cbShame       = findViewById(R.id.cb_shame);
        cbSurprise    = findViewById(R.id.cb_surprise);

        // Apply filters button
        Button applyButton = findViewById(R.id.btn_apply_filters);
        applyButton.setOnClickListener(v -> applyFilters());

        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Google Map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Called when the Google Map is ready to be used.
     * Moves the camera to Edmonton, loads all markers without filters, and sets up
     * a marker click listener to show event details.
     *
     * @param map The GoogleMap instance.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Move camera to Edmonton
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 11.5f));

        // Load markers immediately with no filters (all valid events).
        applyFilters();

        // Set a marker click listener to show a dialog with event details.
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof DocumentSnapshot) {
                DocumentSnapshot docSnap = (DocumentSnapshot) tag;
                showEventDialog(docSnap);
            }
            // Return true to consume the event and prevent the default info window from showing.
            return true;
        });
    }

    /**
     * Retrieves mood events from Firestore based on the selected filter checkboxes,
     * clears existing markers, and adds a new marker for each event with valid location data.
     * Each marker is tagged with its corresponding Firestore DocumentSnapshot.
     */
    private void applyFilters() {
        if (googleMap == null) {
            // Map not yet ready
            return;
        }
        // Clear old markers
        googleMap.clear();

        // Build a Firestore query based on which checkboxes are checked.
        Query query = db.collection("MyPosts");

        // "Last 12 hours" filter
        if (cbLast12Hours.isChecked()) {
            long twelveHoursAgo = System.currentTimeMillis() - (12L * 60L * 60L * 1000L);
            Log.d(TAG, "Applying last 12 hours filter. Time cutoff = " + twelveHoursAgo);
            query = query.whereGreaterThan("timestamp", twelveHoursAgo);
        }

        // Gather selected moods
        List<String> selectedMoods = new ArrayList<>();
        if (cbConfusion.isChecked()) selectedMoods.add("Confusion");
        if (cbAnger.isChecked())     selectedMoods.add("Anger");
        if (cbFear.isChecked())      selectedMoods.add("Fear");
        if (cbDisgust.isChecked())   selectedMoods.add("Disgust");
        if (cbHappy.isChecked())     selectedMoods.add("Happy");
        if (cbSad.isChecked())       selectedMoods.add("Sad");
        if (cbShame.isChecked())     selectedMoods.add("Shame");
        if (cbSurprise.isChecked())  selectedMoods.add("Surprise");

        if (!selectedMoods.isEmpty()) {
            Log.d(TAG, "Applying mood filter: " + selectedMoods);
            query = query.whereIn("mood", selectedMoods);
        }

        // Execute the query
        query.get()
                .addOnSuccessListener(snap -> {
                    Log.d(TAG, "Documents found: " + snap.size());
                    for (DocumentSnapshot doc : snap) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String title = doc.getString("title");
                        String mood  = doc.getString("mood");
                        Log.d(TAG, "Doc ID: " + doc.getId()
                                + ", lat: " + lat + ", lng: " + lng
                                + ", mood: " + mood);

                        // Skip if lat/lng is null or 0
                        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
                            Log.d(TAG, "Skipping document " + doc.getId()
                                    + " due to invalid lat/lng.");
                            continue;
                        }

                        // Add marker for valid coordinates and store the document in the marker's tag.
                        LatLng position = new LatLng(lat, lng);
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(position)
                                .title(title != null ? title : "No Title")
                                .snippet(mood != null ? mood : "No Mood"));
                        marker.setTag(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MoodMap.this,
                            "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Query failed: ", e);
                });
    }

    /**
     * Displays an AlertDialog showing the details of a mood event.
     * The event details are extracted from the given Firestore DocumentSnapshot.
     *
     * @param docSnap The DocumentSnapshot containing the event data.
     */
    private void showEventDialog(DocumentSnapshot docSnap) {
        // Extract fields from the document.
        String title = docSnap.getString("title");
        String mood = docSnap.getString("mood");
        String explanation = docSnap.getString("moodExplanation");
        String situation = docSnap.getString("situation");
        String date = docSnap.getString("date");
        String user = docSnap.getString("postUser");

        // Build the message string.
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Mood: ").append(mood != null ? mood : "N/A").append("\n")
                .append("User: ").append(user != null ? user : "N/A").append("\n")
                .append("Reason: ").append(explanation != null ? explanation : "N/A").append("\n")
                .append("Situation: ").append(situation != null ? situation : "N/A").append("\n")
                .append("Date: ").append(date != null ? date : "N/A").append("\n");

        // Create and show the dialog.
        new AlertDialog.Builder(this)
                .setTitle(title != null ? title : "Mood Event")
                .setMessage(messageBuilder.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

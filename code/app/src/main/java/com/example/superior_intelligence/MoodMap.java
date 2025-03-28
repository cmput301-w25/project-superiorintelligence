package com.example.superior_intelligence;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MoodMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MoodMap";
    private GoogleMap googleMap;
    private FirebaseFirestore db;

    // Filter CheckBoxes
    private CheckBox cbLast12Hours, cbConfusion, cbAnger, cbFear,
            cbDisgust, cbHappy, cbSad, cbShame, cbSurprise, cbMyPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map); // Must contain <fragment> for SupportMapFragment and the filter checkboxes

        db = FirebaseFirestore.getInstance();

        // Find checkboxes (ensure your layout has a CheckBox with id "cb_myposts")
        cbLast12Hours = findViewById(R.id.cb_last_12_hours);
        cbConfusion   = findViewById(R.id.cb_confusion);
        cbAnger       = findViewById(R.id.cb_anger);
        cbFear        = findViewById(R.id.cb_fear);
        cbDisgust     = findViewById(R.id.cb_disgust);
        cbHappy       = findViewById(R.id.cb_happy);
        cbSad         = findViewById(R.id.cb_sad);
        cbShame       = findViewById(R.id.cb_shame);
        cbSurprise    = findViewById(R.id.cb_surprise);
        cbMyPosts     = findViewById(R.id.cb_myposts);

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
     * Moves the camera to Edmonton, loads markers, and sets up a marker click listener.
     *
     * @param map The GoogleMap instance.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Move camera to Edmonton (example location)
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 11.5f));

        // Load markers immediately
        applyFilters();

        // Set a marker click listener to show event details.
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof DocumentSnapshot) {
                DocumentSnapshot docSnap = (DocumentSnapshot) tag;
                showEventDialog(docSnap);
            }
            return true;
        });
    }

    /**
     * Retrieves mood events from Firestore applying filters for time, mood, location (within 5 km),
     * and user. If "My Posts" is checked, only events created by the current user are shown.
     * Otherwise, only events from users that the current user follows are displayed.
     */
    private void applyFilters() {
        if (googleMap == null) {
            return;
        }
        googleMap.clear();

        // Use the camera's target as a proxy for the current location.
        LatLng currentLocation = googleMap.getCameraPosition().target;

        // 1) Build a base query from the "MyPosts" collection, applying time & mood filters.
        Query baseQuery = db.collection("MyPosts");

        // Apply "Last 12 hours" filter.
        if (cbLast12Hours.isChecked()) {
            long twelveHoursAgo = System.currentTimeMillis() - (12L * 60L * 60L * 1000L);
            Log.d(TAG, "Applying last 12 hours filter. Time cutoff = " + twelveHoursAgo);
            baseQuery = baseQuery.whereGreaterThan("timestamp", twelveHoursAgo);
        }

        // Apply mood filter.
        List<String> selectedMoods = new ArrayList<>();
        if (cbConfusion.isChecked()) selectedMoods.add("Confusion");
        if (cbAnger.isChecked())     selectedMoods.add("Anger");
        if (cbFear.isChecked())      selectedMoods.add("Fear");
        if (cbDisgust.isChecked())   selectedMoods.add("Disgust");
        if (cbHappy.isChecked())     selectedMoods.add("Happiness");
        if (cbSad.isChecked())       selectedMoods.add("Sadness");
        if (cbShame.isChecked())     selectedMoods.add("Shame");
        if (cbSurprise.isChecked())  selectedMoods.add("Surprise");

        if (!selectedMoods.isEmpty()) {
            Log.d(TAG, "Applying mood filter: " + selectedMoods);
            baseQuery = baseQuery.whereIn("mood", selectedMoods);
        }

        // 2) Apply the user filter (my posts vs. followed users).
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (cbMyPosts.isChecked()) {
            // Show only the current user's events
            Query myQuery = baseQuery.whereEqualTo("postUser", currentUserId);
            Log.d(TAG, "Filtering to show only current user's posts.");
            executeQuery(myQuery, currentLocation);
        } else {
            // By default, show events from followed users.
            Query finalBaseQuery = baseQuery;
            Userbase.getInstance().getUserFollowing(currentUserId, followedUsers -> {
                if (followedUsers == null || followedUsers.isEmpty()) {
                    Log.d(TAG, "No followed users found; no events will be shown.");
                    return;
                }
                // Derive a new query from baseQuery with the 'whereIn' constraint.
                Query followedQuery = finalBaseQuery.whereIn("postUser", followedUsers);
                Log.d(TAG, "Filtering to show events from followed users: " + followedUsers);
                executeQuery(followedQuery, currentLocation);
            });
        }
    }

    /**
     * Executes the Firestore query and processes the results.
     *
     * @param query           The Firestore query to execute.
     * @param currentLocation The current location (as a LatLng) to filter events within 5 km.
     */
    private void executeQuery(Query query, LatLng currentLocation) {
        query.get()
                .addOnSuccessListener(snap -> {
                    Log.d(TAG, "Documents found: " + snap.size());
                    for (DocumentSnapshot doc : snap) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String mood = doc.getString("mood");
                        String user = doc.getString("postUser");

                        Log.d(TAG, "Doc ID: " + doc.getId()
                                + ", lat: " + lat + ", lng: " + lng
                                + ", mood: " + mood);

                        // Skip if lat/lng is null or invalid.
                        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
                            Log.d(TAG, "Skipping document " + doc.getId() + " due to invalid lat/lng.");
                            continue;
                        }

                        // Check if event is within 5 km of the current location.
                        float[] distanceResult = new float[1];
                        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                                lat, lng, distanceResult);
                        if (distanceResult[0] > 5000) { // 5000 meters = 5 km
                            Log.d(TAG, "Event " + doc.getId() + " is " + distanceResult[0] + " meters away; skipping event.");
                            continue;
                        }

                        LatLng position = new LatLng(lat, lng);

                        // Prepare marker title and snippet.
                        String markerTitle = (user != null) ? user : "Unknown User";
                        String markerSnippet = (mood != null) ? "Mood: " + mood : "No Mood";

                        MarkerOptions options = new MarkerOptions()
                                .position(position)
                                .title(markerTitle)
                                .snippet(markerSnippet)
                                .anchor(0.5f, 1.0f);

                        int iconRes = getMoodMarkerIcon(mood);
                        if (iconRes != -1) {
                            BitmapDescriptor labeledIcon = createLabeledMarker(iconRes, markerTitle);
                            options.icon(labeledIcon);
                        }

                        Marker marker = googleMap.addMarker(options);
                        if (marker != null) {
                            marker.setTag(doc);
                        }
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
     * Creates a custom marker icon by combining the mood icon and the username text.
     *
     * @param baseIconRes The drawable resource ID for the base (mood) icon.
     * @param label       The username text to render.
     * @return A BitmapDescriptor of the combined image.
     */
    private BitmapDescriptor createLabeledMarker(int baseIconRes, String label) {
        // Decode the base icon.
        Bitmap original = BitmapFactory.decodeResource(getResources(), baseIconRes);

        // Scale the icon to a desired size.
        int iconWidth = 150;
        int iconHeight = 150;
        Bitmap scaledIcon = Bitmap.createScaledBitmap(original, iconWidth, iconHeight, false);

        // Create a bitmap with extra space for text.
        int extraHeight = 60;
        Bitmap combined = Bitmap.createBitmap(iconWidth, iconHeight + extraHeight, Bitmap.Config.ARGB_8888);

        // Draw the icon and text.
        Canvas canvas = new Canvas(combined);
        canvas.drawBitmap(scaledIcon, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        paint.setTextAlign(Paint.Align.CENTER);

        float xPos = iconWidth / 2f;
        float yPos = iconHeight + 40f;
        canvas.drawText(label, xPos, yPos, paint);

        return BitmapDescriptorFactory.fromBitmap(combined);
    }

    /**
     * Displays an AlertDialog showing the details of a mood event.
     *
     * @param docSnap The DocumentSnapshot containing the event data.
     */
    private void showEventDialog(DocumentSnapshot docSnap) {
        String title = docSnap.getString("title");
        String mood = docSnap.getString("mood");
        String explanation = docSnap.getString("moodExplanation");
        String situation = docSnap.getString("situation");
        String date = docSnap.getString("date");
        String user = docSnap.getString("postUser");

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Mood: ").append(mood != null ? mood : "N/A").append("\n")
                .append("User: ").append(user != null ? user : "N/A").append("\n")
                .append("Reason: ").append(explanation != null ? explanation : "N/A").append("\n")
                .append("Situation: ").append(situation != null ? situation : "N/A").append("\n")
                .append("Date: ").append(date != null ? date : "N/A").append("\n");

        new AlertDialog.Builder(this)
                .setTitle(title != null ? title : "Mood Event")
                .setMessage(messageBuilder.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Returns the drawable resource ID for the given mood string, or -1 if unrecognized.
     *
     * @param mood The mood string from Firestore.
     * @return The drawable resource ID, or -1 if not recognized.
     */
    private int getMoodMarkerIcon(String mood) {
        if (mood == null) {
            return -1;
        }
        switch (mood.toLowerCase()) {
            case "confusion":
                return R.drawable.confusion_mm;
            case "anger":
                return R.drawable.angry_mm;
            case "fear":
                return R.drawable.fear_mm;
            case "disgust":
                return R.drawable.disgust_mm;
            case "happiness":
                return R.drawable.happy_mm;
            case "sadness":
                return R.drawable.sadness_mm;
            case "shame":
                return R.drawable.shame_mm;
            case "surprise":
                return R.drawable.surprise_mm;
            default:
                return -1;
        }
    }
}

package com.example.superior_intelligence;

import android.content.Context;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch; // Added import for CountDownLatch

/**
 * MoodMap activity that displays mood events on a map.
 * - If "My Posts" is checked, shows ALL of the current user's events.
 * - Otherwise, shows ONLY the single most recent event for each followed user,
 *   filtered to within 5 km from the current map center.
 */
public class MoodMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MoodMap";
    public GoogleMap googleMap;
    private FirebaseFirestore db;

    // Filter CheckBoxes
    private CheckBox cbConfusion, cbAnger, cbFear,
            cbDisgust, cbHappy, cbSad, cbShame, cbSurprise, cbMyPosts;

    // Added for testing: List to store markers.
    private List<Marker> markers = new ArrayList<>();

    private final List<MoodClusterItem> clusterItems = new ArrayList<>();

    // Manages markers clustered together on mood map for really close locations.
    private ClusterManager<MoodClusterItem> clusterManager;


    // Added for test synchronization: Latch to signal when markers are loaded.
    public CountDownLatch markersLatch; // Public so tests can set it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map); // Must contain <fragment> for SupportMapFragment and filter checkboxes

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        // Find checkboxes
        cbConfusion   = findViewById(R.id.cb_confusion);
        cbAnger       = findViewById(R.id.cb_anger);
        cbFear        = findViewById(R.id.cb_fear);
        cbDisgust     = findViewById(R.id.cb_disgust);
        cbHappy       = findViewById(R.id.cb_happy);
        cbSad         = findViewById(R.id.cb_sadness);
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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Move camera to Edmonton
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 15f));

        // Enable map gestures and controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Initialize cluster manager
        clusterManager = new ClusterManager<>(this, googleMap);
        clusterManager.setRenderer(new MoodClusterRenderer(this, googleMap, clusterManager));

        // Set listeners for clustering behavior
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        // Optional: handle individual mood item clicks
        clusterManager.setOnClusterItemClickListener(item -> {
            new AlertDialog.Builder(this)
                    .setTitle(item.getTitle())
                    .setMessage(item.getSnippet())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });

        // Load markers only if a filter is applied
        applyFilters();
    }

    /**
     * Main filter logic:
     * 1) If "My Posts" is checked, show ALL events from the current user.
     * 2) Otherwise, show ONLY the single most recent event for each followed user,
     *    within 5 km of the current map center.
     */
    private void applyFilters() {
        if (googleMap == null) {
            return;
        }
        googleMap.clear();
        // Clear our markers list as well.
        markers.clear();

        // Get the current user from your app's singleton
        User currentUser = User.getInstance();
        if (currentUser == null) {
            Log.e(TAG, "No current user found (User.getInstance() == null). Cannot filter events.");
            Toast.makeText(this, "No current user available.", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUsername = currentUser.getUsername();
        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e(TAG, "Current user has no username set!");
            Toast.makeText(this, "No username found for current user.", Toast.LENGTH_SHORT).show();
            return;
        }

        // We'll use the map center as a proxy for the user's current location
        LatLng currentLocation = googleMap.getCameraPosition().target;

        // Base Firestore query
        Query baseQuery = db.collection("MyPosts");

        // 2) Mood filters
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

        // 3) My Posts vs. Followed Users
        if (cbMyPosts.isChecked()) {
            // Show ALL of my events
            Query myQuery = baseQuery.whereEqualTo("postUser", currentUsername);
            Log.d(TAG, "Showing all events by user: " + currentUsername);
            // We can reuse the existing "executeQuery" method to place all markers
            executeQuery(myQuery, currentLocation);

        } else {
            // Show only the single most recent event for each followed user
            Query finalBaseQuery = baseQuery;
            Userbase.getInstance().getUserFollowing(currentUsername, followedUsers -> {
                if (followedUsers == null || followedUsers.isEmpty()) {
                    Log.d(TAG, "No followed users found; no events will be shown.");
                    return;
                }
                Log.d(TAG, "Filtering to show single most recent event from followed users: " + followedUsers);

                // Build a query for all events by followed users
                Query followedQuery = finalBaseQuery.whereIn("postUser", followedUsers);

                // Now fetch those events and group by user, picking only the most recent
                followedQuery.get()
                        .addOnSuccessListener(snap -> {
                            Log.d(TAG, "Documents found: " + snap.size());

                            // We'll store only the single doc with the largest timestamp per user
                            Map<String, DocumentSnapshot> latestByUser = new HashMap<>();

                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                String postUser = doc.getString("postUser");
                                if (postUser == null) continue;

                                Long docTimestamp = doc.getLong("timestamp");
                                if (docTimestamp == null) docTimestamp = 0L;

                                // Keep track of the doc with the largest timestamp for this user
                                if (!latestByUser.containsKey(postUser)) {
                                    latestByUser.put(postUser, doc);
                                } else {
                                    DocumentSnapshot existing = latestByUser.get(postUser);
                                    Long existingTs = existing.getLong("timestamp");
                                    if (existingTs == null) existingTs = 0L;

                                    if (docTimestamp > existingTs) {
                                        latestByUser.put(postUser, doc);
                                    }
                                }
                            }

                            // Now place a marker only for the single doc per user
                            // if it's within 5 km of currentLocation
                            for (DocumentSnapshot doc : latestByUser.values()) {
                                placeMarkerIfWithinRange(doc, currentLocation);
                            }

                            // Signal that markers are loaded
                            if (markersLatch != null) {
                                markersLatch.countDown();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MoodMap.this,
                                    "Error loading events: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Query failed: ", e);
                            // Signal even on failure to prevent test deadlock
                            if (markersLatch != null) {
                                markersLatch.countDown();
                            }
                        });
            });
        }
    }

    /**
     * Places a marker for the given doc if it's within 5 km of the given location.
     * This is used for the "single most recent event" approach in the else branch.
     */
    private void placeMarkerIfWithinRange(DocumentSnapshot doc, LatLng currentLocation) {
        Double lat = doc.getDouble("lat");
        Double lng = doc.getDouble("lng");
        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
            Log.d(TAG, "Skipping doc " + doc.getId() + " due to invalid lat/lng.");
            return;
        }

        float[] distanceResult = new float[1];
        Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                lat, lng, distanceResult
        );
        if (distanceResult[0] > 5000) {
            Log.d(TAG, "Event " + doc.getId() + " is " + distanceResult[0] + "m away; skipping.");
            return;
        }

        String mood = doc.getString("mood");
        String user = doc.getString("postUser");
        String markerTitle = (user != null) ? user : "Unknown User";
        String markerSnippet = (mood != null) ? "Mood: " + mood : "No Mood";

        // Add to cluster
        int iconRes = getMoodMarkerIcon(mood);
        if (iconRes == -1) return; // skip unknown moods
        MoodClusterItem item = new MoodClusterItem(lat, lng, markerTitle, markerSnippet, iconRes);
        clusterItems.add(item);  // Track for testing
        clusterManager.addItem(item);
        clusterManager.cluster();

    }

    /**
     * Executes the Firestore query and processes the results (including a 5 km distance filter).
     * This method places ALL docs that match the query (for "my posts" scenario).
     */
    private void executeQuery(Query query, LatLng currentLocation) {
        query.get()
                .addOnSuccessListener(snap -> {
                    Log.d(TAG, "Documents found: " + snap.size());
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String mood = doc.getString("mood");
                        String user = doc.getString("postUser");

                        Log.d(TAG, "Doc ID: " + doc.getId()
                                + ", lat: " + lat + ", lng: " + lng
                                + ", mood: " + mood);

                        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
                            Log.d(TAG, "Skipping doc " + doc.getId() + " due to invalid lat/lng.");
                            continue;
                        }

                        float[] distanceResult = new float[1];
                        Location.distanceBetween(
                                currentLocation.latitude, currentLocation.longitude,
                                lat, lng, distanceResult
                        );
                        if (distanceResult[0] > 5000) {
                            Log.d(TAG, "Event " + doc.getId() + " is " + distanceResult[0] + " meters away; skipping.");
                            continue;
                        }

                        String markerTitle = (user != null) ? user : "Unknown User";
                        String markerSnippet = (mood != null) ? "Mood: " + mood : "No Mood";

                        // Add to cluster
                        int iconRes = getMoodMarkerIcon(mood);
                        if (iconRes == -1) return; // skip unknown moods
                        MoodClusterItem item = new MoodClusterItem(lat, lng, markerTitle, markerSnippet, iconRes);
                        clusterManager.addItem(item);
                        clusterItems.add(item);
                    }
                    clusterManager.cluster();


                    if (markersLatch != null) {
                        markersLatch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MoodMap.this,
                            "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Query failed: ", e);
                    if (markersLatch != null) {
                        markersLatch.countDown();
                    }
                });
    }

    /**
     * Returns the drawable resource ID for the given mood string, or -1 if unrecognized.
     */
    public static int getMoodMarkerIcon(String mood) {
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

    /**
     * Calculates the distance between two coordinates using the Haversine formula.
     * @return distance in kilometers
     */
    public static float distanceInKilometers(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371; // Approximate radius of Earth in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (EARTH_RADIUS_KM * c);
    }

    public static boolean isWithinRange(double lat1, double lon1, double lat2, double lon2, float maxDistanceKm) {
        return distanceInKilometers(lat1, lon1, lat2, lon2) <= maxDistanceKm;
    }

    public class MoodClusterItem implements ClusterItem {
        private final LatLng position;
        private final String title;
        private final String snippet;
        private final int iconResId;

        public MoodClusterItem(double lat, double lng, String title, String snippet, int iconResId) {
            this.position = new LatLng(lat, lng);
            this.title = title;
            this.snippet = snippet;
            this.iconResId = iconResId;
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return position;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getSnippet() {
            return snippet;
        }

        @Nullable
        @Override
        public Float getZIndex() {
            return 0.0f;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    class MoodClusterRenderer extends DefaultClusterRenderer<MoodClusterItem> {
        private final Context context;

        public MoodClusterRenderer(Context context, GoogleMap map, ClusterManager<MoodClusterItem> clusterManager) {
            super(context, map, clusterManager);
            this.context = context;
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull MoodClusterItem item, @NonNull MarkerOptions markerOptions) {
            Bitmap original = BitmapFactory.decodeResource(context.getResources(), item.getIconResId());
            Bitmap scaled = Bitmap.createScaledBitmap(original, 100, 100, false); // Resize to 100x100 px
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(scaled);

            markerOptions.icon(icon)
                    .title(item.getTitle())
                    .snippet(item.getSnippet());

        }
    }

    public List<MoodClusterItem> getClusterItems() {
        return clusterItems;
    }

}
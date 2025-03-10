package com.example.superior_intelligence;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.ImageButton;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Query;
//import com.mapbox.geojson.Point;
//import com.mapbox.maps.MapView;
//import com.mapbox.maps.Style;
//import com.mapbox.maps.plugin.Plugin;
//import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MoodMap extends AppCompatActivity {
//
//    private FirebaseFirestore db;
//
//    // Mapbox MapView & Annotation Manager
//    private MapView mapView;
//    private PointAnnotationManager pointAnnotationManager;
//
//    // Filter CheckBoxes
//    private CheckBox cbLast12Hours;
//    private CheckBox cbConfusion;
//    private CheckBox cbAnger;
//    private CheckBox cbFear;
//    private CheckBox cbDisgust;
//    private CheckBox cbHappy;
//    private CheckBox cbFollowing; // If you have a 'Following' checkbox in the layout
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // 1) Use the layout containing <com.mapbox.maps.MapView android:id="@+id/mapView" ... />
//        setContentView(R.layout.map);
//
//        // 2) Firestore instance
//        db = FirebaseFirestore.getInstance();
//
//        // 3) Back button
//        ImageButton backButton = findViewById(R.id.back_button);
//        backButton.setOnClickListener(v -> finish());
//
//        // 4) CheckBoxes for filtering
//        cbLast12Hours = findViewById(R.id.cb_last_12_hours);
//        cbConfusion   = findViewById(R.id.cb_confusion);
//        cbAnger       = findViewById(R.id.cb_anger);
//        cbFear        = findViewById(R.id.cb_fear);
//        cbDisgust     = findViewById(R.id.cb_disgust);
//        cbHappy       = findViewById(R.id.cb_happy);
//        // If you actually have this in XML, uncomment:
//        // cbFollowing   = findViewById(R.id.cb_following);
//
//        // 5) "Apply" button for filters
//        Button applyButton = findViewById(R.id.btn_apply_filters);
//        applyButton.setOnClickListener(v -> applyFilters());
//
//        // 6) Initialize MapView
//        mapView = findViewById(R.id.mapView);
//
//        // 7) Load a Mapbox style, then create an annotation manager for markers
//        mapView.getMapboxMap().loadStyleUri(
//                Style.MAPBOX_STREETS,
//                style -> {
//                    // Acquire the annotation plugin
//                    AnnotationPlugin annotationPlugin =
//                            (AnnotationPlugin) mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
//
//                    // Create a PointAnnotationManager (no arguments needed in v10+/v11+)
////                    pointAnnotationManager = annotationPlugin.createPointAnnotationManager();
//
//                    // If you want to load data by default, call applyFilters() here
//                    // applyFilters();
//                }
//        );
//    }
//
//    /**
//     * Builds a Firestore query based on checkboxes and fetches data.
//     * Then calls displayPostsOnMap() to show markers.
//     */
//    private void applyFilters() {
//        Query query = db.collection("MyPosts");
//
//        // 1) Last 12 hours
//        if (cbLast12Hours.isChecked()) {
//            long twelveHoursAgo = System.currentTimeMillis() - (12 * 60 * 60 * 1000);
//            query = query.whereGreaterThan("timestamp", twelveHoursAgo);
//        }
//
//        // 2) Moods
//        List<String> selectedMoods = new ArrayList<>();
//        if (cbConfusion.isChecked()) selectedMoods.add("confusion");
//        if (cbAnger.isChecked())     selectedMoods.add("anger");
//        if (cbFear.isChecked())      selectedMoods.add("fear");
//        if (cbDisgust.isChecked())   selectedMoods.add("disgust");
//        if (cbHappy.isChecked())     selectedMoods.add("happy");
//        // Add more as needed
//
//        if (!selectedMoods.isEmpty()) {
//            query = query.whereIn("mood", selectedMoods);
//        }
//
//        // 3) Following filter
//        if (cbFollowing != null && cbFollowing.isChecked()) {
//            query = query.whereEqualTo("isFollowed", true);
//        }
//
//        // 4) Execute the query
//        query.get().addOnSuccessListener(snapshot -> {
//            List<Event> posts = new ArrayList<>();
//            for (DocumentSnapshot doc : snapshot.getDocuments()) {
//                Event event = doc.toObject(Event.class);
//                if (event != null) {
//                    posts.add(event);
//                }
//            }
//            // Show them on the map
//            displayPostsOnMap(posts);
//        }).addOnFailureListener(e -> {
//            e.printStackTrace();
//            // handle error
//        });
//    }
//
//    /**
//     * Clears existing markers and adds new ones for each Event with lat/lng.
//     */
//    private void displayPostsOnMap(List<Event> posts) {
//        // If the map style or annotation manager isn't ready, do nothing
//        if (pointAnnotationManager == null) {
//            return;
//        }
//
//        // Clear old markers
//        pointAnnotationManager.deleteAll();
//
//        if (posts.isEmpty()) {
//            return;
//        }
//
//        Double firstLat = null;
//        Double firstLng = null;
//
//        // Create a list of annotation options
//        List<PointAnnotationOptions> annotationOptionsList = new ArrayList<>();
//
//        for (Event event : posts) {
//            Double lat = event.getLat();
//            Double lng = event.getLng();
//            if (lat != null && lng != null) {
//                if (firstLat == null) {
//                    firstLat = lat;
//                    firstLng = lng;
//                }
//
//                String mood = (event.getMood() != null) ? event.getMood() : "Unknown Mood";
//                String title = (event.getTitle() != null) ? event.getTitle() : "";
//
//                // Build an annotation
//                PointAnnotationOptions options = new PointAnnotationOptions()
//                        .withPoint(Point.fromLngLat(lng, lat))
//                        .withTextField(mood)
//                        .withTextSize(12.0f);
//
//                // Add to list
//                annotationOptionsList.add(options);
//            }
//        }
//
//        // Add all markers to the map
//        pointAnnotationManager.create(annotationOptionsList);
//
//        // Optionally move camera to the first post
//        if (firstLat != null && firstLng != null) {
//            mapView.getMapboxMap().setCamera(
//                    new com.mapbox.maps.CameraOptions.Builder()
//                            .center(Point.fromLngLat(firstLng, firstLat))
//                            .zoom(12.0)
//                            .build()
//            );
//        }
//    }
//}

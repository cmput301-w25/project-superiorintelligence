package com.example.superior_intelligence;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MoodMap extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FirebaseFirestore db;

    // Filter CheckBoxes
    private CheckBox cbLast12Hours, cbConfusion, cbAnger, cbFear,
            cbDisgust, cbHappy, cbSad, cbShame, cbSurprise;

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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Move camera to Edmonton
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 11f));
    }

    private void applyFilters() {
        if (googleMap == null) {
            // Map not yet ready
            return;
        }
        // Clear old markers
        googleMap.clear();

        Query query = db.collection("MyPosts");

        // "Last 12 hours" filter
        if (cbLast12Hours.isChecked()) {
            long twelveHoursAgo = System.currentTimeMillis() - (12L * 60L * 60L * 1000L);
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
            query = query.whereIn("mood", selectedMoods);
        }

        query.get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat != null && lng != null) {
                            String title = doc.getString("title");
                            String mood  = doc.getString("mood");

                            LatLng position = new LatLng(lat, lng);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(title != null ? title : "No Title")
                                    .snippet(mood != null ? mood : "No Mood"));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MoodMap.this,
                                "Error loading events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}

package com.example.superior_intelligence;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private Database database;

    // Separate lists for each tab
    private List<Event> exploreEvents = new ArrayList<>();
    private List<Event> followedEvents = new ArrayList<>();
    private List<Event> myPostsEvents = new ArrayList<>();

    private TextView tabExplore, tabFollowed, tabMyPosts, tabMap;
    private ImageButton filterButton;

    // We'll display the user’s profile photo in this ImageView
    private CardView profileImageCard;
    private SharedPreferences prefs;
    private FirebaseFirestore db;

    private String currentTextFilter = null;
    private String currentTab = null;

    private final ActivityResultLauncher<Intent> viewDetailsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String incomingFilter = data.getStringExtra("textFilter");
                    if (incomingFilter != null) {
                        currentTextFilter = incomingFilter;
                    }
                    String selectedTab = data.getStringExtra("selectedTab");
                    if (selectedTab != null) {
                        data.putExtra("selectedTab", selectedTab);
                    }
                    setIntent(data);
                    loadAllEvents(this::handleIncomingEvent);
                }
            });

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("AuthDebug", "No user session found! Redirecting to login.");
            Intent loginIntent = new Intent(this, LoginPageActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        } else {
            Log.d("AuthDebug", "User logged in: " + currentUser.getEmail());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        database = new Database();

        // RecyclerView + Adapter
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, event -> {
            Intent intent = new Intent(HomeActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("textFilter", currentTextFilter);
            viewDetailsLauncher.launch(intent);
        });
        recyclerView.setAdapter(adapter);

        // Category Tabs
        tabExplore = findViewById(R.id.tab_explore);
        tabFollowed = findViewById(R.id.tab_followed);
        tabMyPosts = findViewById(R.id.tab_myposts);
        tabMap = findViewById(R.id.tab_map);

        // Filter Button + Spinner
        filterButton = findViewById(R.id.menu_button);
        Spinner filterSpinner = findViewById(R.id.filter_spinner);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        filterButton.setOnClickListener(v -> {
            if (filterSpinner.getVisibility() == View.GONE) {
                filterSpinner.setSelection(0, false);
                filterSpinner.setVisibility(View.VISIBLE);
                filterSpinner.performClick();
            } else {
                filterSpinner.setVisibility(View.GONE);
            }
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                if (position == 0) return; // Skip dummy item
                filterSpinner.setSelection(0, false);
                filterSpinner.setVisibility(View.GONE);

                switch (selectedFilter) {
                    case "Filter by text":
                        showTextFilterDialog();
                        break;
                    case "Filter by emotional state":
                        showEmotionFilterDialog();
                        break;
                    case "Show posts from last 7 days":
                        if ("myposts".equals(currentTab)) {
                            try {
                                recentWeek(myPostsEvents);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        } else if ("followed".equals(currentTab)) {
                            try {
                                recentWeek(followedEvents);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    case "Clear filter":
                        currentTextFilter = null;
                        if ("myposts".equals(currentTab)) {
                            adapter.setEvents(myPostsEvents);
                        } else if ("followed".equals(currentTab)) {
                            adapter.setEvents(followedEvents);
                        } else if ("explore".equals(currentTab)) {
                            adapter.setEvents(exploreEvents);
                        }
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Tab listeners
        tabExplore.setOnClickListener(v -> switchTab(exploreEvents, tabExplore));
        tabFollowed.setOnClickListener(v -> switchTab(followedEvents, tabFollowed));
        tabMyPosts.setOnClickListener(v -> switchTab(myPostsEvents, tabMyPosts));
        tabMap.setOnClickListener(v -> {
            switchTab(new ArrayList<>(), tabMap);
            startActivity(new Intent(HomeActivity.this, MoodMap.class));
        });

        // Floating Add Button
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MoodCreateAndEditActivity.class)));

        // Profile Icon + Notification
        profileImageCard = findViewById(R.id.profile_image);
        profileImageCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        ImageButton notificationButton = findViewById(R.id.notification_button);
        ActivityResultLauncher<Intent> notificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        setIntent(data);
                        handleIncomingEvent();
                    }
                }
        );
        notificationButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            intent.putExtra("selectedTab", currentTab);
            intent.putExtra("textFilter", currentTextFilter);
            notificationLauncher.launch(intent);
        });

        // Load events
        loadAllEvents(this::handleIncomingEvent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload events in case something changed
        loadAllEvents(this::handleIncomingEvent);

        // Also load or refresh the user’s profile photo
        loadProfilePhotoForHome();
    }

    /**
     * Attempt to load the user's profile photo from SharedPreferences.
     * If none is found, load from Firestore's "profile_photo" collection.
     */
    private void loadProfilePhotoForHome() {
        User currentUser = User.getInstance();
        if (currentUser == null) {
            Log.e(TAG, "No current user; cannot load profile photo.");
            return;
        }
        String username = currentUser.getUsername();
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Username is empty; cannot load profile photo.");
            return;
        }

        // Check local
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String encodedPhoto = prefs.getString("photo", null);
        if (encodedPhoto != null) {
            loadProfilePhotoFromBase64(encodedPhoto);
        } else {
            // Otherwise fetch from Firestore
            loadProfilePhotoFromFirestore(username);
        }
    }

    /**
     * Load the Base64-encoded photo into the top-right icon.
     */
    private void loadProfilePhotoFromBase64(String encodedImage) {
        ImageView profilePng = findViewById(R.id.profile_image_png);
        try {
            byte[] imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profilePng.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode profile image for HomeActivity.", e);
        }
    }

    /**
     * Load the user's profile photo from Firestore "profile_photo/{username}" doc if not in SharedPreferences.
     */
    private void loadProfilePhotoFromFirestore(String username) {
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
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load profile photo from Firestore in HomeActivity", e));
    }

    // -----------------------------------------
    // Existing methods from your HomeActivity
    // -----------------------------------------

    private void handleIncomingEvent() {
        Intent intent = getIntent();
        Event newEvent = (Event) intent.getSerializableExtra("newEvent");
        if (newEvent != null && newEvent.isMyPost()) {
            boolean found = false;
            for (int i = 0; i < myPostsEvents.size(); i++) {
                if (myPostsEvents.get(i).getID().equals(newEvent.getID())) {
                    myPostsEvents.set(i, newEvent);
                    found = true;
                    break;
                }
            }
            if (!found) {
                myPostsEvents.add(newEvent);
            }
        }

        String selectedTab = intent.getStringExtra("selectedTab");
        if ("myposts".equals(selectedTab)) {
            String preservedFilter = intent.getStringExtra("textFilter");
            if (preservedFilter != null) {
                currentTextFilter = preservedFilter;
            }
            switchTab(myPostsEvents, tabMyPosts);
        } else if ("followed".equals(selectedTab)) {
            switchTab(followedEvents, tabFollowed);
        } else {
            // default to EXPLORE
            switchTab(exploreEvents, tabExplore);
        }

        String deletedEventId = intent.getStringExtra("deletedEventId");
        if (deletedEventId != null) {
            myPostsEvents.removeIf(event -> event.getID().equals(deletedEventId));
        }
    }

    private void loadAllEvents(Runnable afterLoad) {
        User currentUser = User.getInstance();
        if (currentUser == null) {
            Log.e("UserDebug", "User instance is NULL! Skipping event loading.");
            return;
        }
        database.loadEventsFromFirebase(currentUser, (myPosts, explore, followed) -> {
            if (myPosts != null && explore != null && followed != null) {
                myPostsEvents.clear();
                exploreEvents.clear();
                followedEvents.clear();

                myPosts.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                explore.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                followed.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));

                myPostsEvents.addAll(myPosts);
                exploreEvents.addAll(explore);
                followedEvents.addAll(followed);

                adapter.notifyDataSetChanged();

                if (afterLoad != null) {
                    afterLoad.run();
                }
            } else {
                Log.w("HomeActivity", "Failed to load events from Firestore");
            }
        });
    }

    private void switchTab(List<Event> targetList, TextView selectedTabView) {
        currentTab = selectedTabView.getText().toString().toLowerCase();

        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);

        selectedTabView.setTypeface(null, android.graphics.Typeface.BOLD);

        if ("explore".equals(currentTab)) {
            filterButton.setEnabled(false);
            filterButton.setAlpha(0.3f);
        } else {
            filterButton.setEnabled(true);
            filterButton.setAlpha(1f);
        }

        if (("myposts".equals(currentTab) || "followed".equals(currentTab)) &&
                currentTextFilter != null && !currentTextFilter.isEmpty()) {
            filterEventsByReason(currentTextFilter);
        } else {
            adapter.setEvents(targetList);
        }
    }

    private void filterEventsByReason(String keyword) {
        if (keyword.isEmpty()) {
            if ("myposts".equals(currentTab)) {
                switchTab(myPostsEvents, tabMyPosts);
            } else if ("followed".equals(currentTab)) {
                switchTab(followedEvents, tabFollowed);
            }
            return;
        }

        List<Event> sourceList = null;
        if ("myposts".equals(currentTab)) {
            sourceList = myPostsEvents;
        } else if ("followed".equals(currentTab)) {
            sourceList = followedEvents;
        }
        if (sourceList == null) return;

        String lowerKeyword = keyword.toLowerCase();
        List<Event> exactMatches = new ArrayList<>();
        List<Event> partialMatches = new ArrayList<>();

        for (Event e : sourceList) {
            String reason = e.getMoodExplanation() != null ? e.getMoodExplanation().toLowerCase() : "";
            if (reason.matches(".*\\b" + Pattern.quote(lowerKeyword) + "\\b.*")) {
                exactMatches.add(e);
            } else if (reason.contains(lowerKeyword)) {
                partialMatches.add(e);
            }
        }

        exactMatches.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        partialMatches.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));

        List<Event> filteredList = new ArrayList<>(exactMatches);
        filteredList.addAll(partialMatches);
        adapter.setEvents(filteredList);
    }

    private void showTextFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.DialogTheme);
        builder.setTitle("Enter search phrase");

        final EditText input = new EditText(HomeActivity.this);
        builder.setView(input);

        builder.setPositiveButton("Filter", (dialog, which) -> {
            String keyword = input.getText().toString().trim().toLowerCase();
            currentTextFilter = keyword;
            filterEventsByReason(keyword);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    private void showEmotionFilterDialog() {
        final String[] moodOptions = getResources().getStringArray(R.array.emotional_state_list);
        final boolean[] selectedMoods = new boolean[moodOptions.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(getString(R.string.select_emotion_prompt));

        builder.setMultiChoiceItems(moodOptions, selectedMoods, (dialog, which, isChecked) -> {
            selectedMoods[which] = isChecked;
        });

        builder.setPositiveButton("Filter", (dialog, which) -> {
            List<String> chosenMoods = new ArrayList<>();
            for (int i = 0; i < moodOptions.length; i++) {
                if (selectedMoods[i]) {
                    chosenMoods.add(moodOptions[i]);
                }
            }
            if ("myposts".equals(currentTab)) {
                filterPostsByMood(chosenMoods, myPostsEvents);
            } else if ("followed".equals(currentTab)) {
                filterPostsByMood(chosenMoods, followedEvents);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    private void filterPostsByMood(List<String> chosenMoods, List<Event> allPosts) {
        if (chosenMoods.isEmpty()) {
            if (allPosts == myPostsEvents) {
                switchTab(myPostsEvents, tabMyPosts);
            } else if (allPosts == followedEvents) {
                switchTab(followedEvents, tabFollowed);
            }
            return;
        }
        List<Event> filteredList = new ArrayList<>();
        for (Event event : allPosts) {
            if (chosenMoods.contains(event.getMood())) {
                filteredList.add(event);
            }
        }
        filteredList.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        adapter.setEvents(filteredList);

        if (allPosts == myPostsEvents) {
            switchTab(filteredList, tabMyPosts);
        } else if (allPosts == followedEvents) {
            switchTab(filteredList, tabFollowed);
        }
    }

    private void recentWeek(List<Event> posts) throws ParseException {
        List<Event> recentWeekEvents = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Date currentDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-6);
        Date recentWeekDate = cal.getTime();

        for (Event e : posts) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH);
            Date eventDate = sdf.parse(e.getDate());
            if (!eventDate.before(recentWeekDate) && !eventDate.after(currentDate)) {
                recentWeekEvents.add(e);
            }
        }
        recentWeekEvents.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        adapter.setEvents(recentWeekEvents);
    }

//    private void filterPostsByMood(List<String> chosenMoods, List<Event> allPosts) {
//        // (already implemented above; ensure no duplication)
//    }
}

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private Database database;

    // Separate lists for each tab
    private List<Event> exploreEvents = new ArrayList<>();
    private List<Event> followedEvents = new ArrayList<>();
    private List<Event> myPostsEvents = new ArrayList<>();

    private TextView tabExplore, tabFollowed, tabMyPosts, tabMap;

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
                        data.putExtra("selectedTab", selectedTab); // persist
                    }

                    setIntent(data); // Save into activity
                    loadAllEvents(() -> {
                        handleIncomingEvent(); // Apply after loading finishes
                    });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        database = new Database();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, event -> {
            Intent intent = new Intent(HomeActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("textFilter", currentTextFilter);
            viewDetailsLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);

        // Tab UI elements
        tabExplore = findViewById(R.id.tab_explore);
        tabFollowed = findViewById(R.id.tab_followed);
        tabMyPosts = findViewById(R.id.tab_myposts);
        tabMap = findViewById(R.id.tab_map);
        ImageButton filterButton = findViewById(R.id.menu_button);
        Spinner filterSpinner = findViewById(R.id.filter_spinner);

        /**
         * Initializes the Spinner with filter options and sets up the filter button to toggle its visibility.
         * This portion configures a Spinner. The Spinner is initially hidden and becomes visible when the user taps
         * the filter button. Tapping the button again hides the Spinner.
         * Creates an ArrayAdapter using the filter_options array.
         * Applies a standard dropdown layout to the Spinner items.
         * Sets the adapter on the Spinner to populate its options.
         * Defines the OnClickListener for the filter button to toggle the Spinner.
         */
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        filterButton.setOnClickListener(v -> {
            if (filterSpinner.getVisibility() == View.GONE) {
                // Reset to 0 before making visible to ensure selection triggers again
                filterSpinner.setSelection(0, false);
                filterSpinner.setVisibility(View.VISIBLE);
                filterSpinner.performClick(); // Open dropdown
            } else {
                filterSpinner.setVisibility(View.GONE);
            }
        });

        /**
         * Sets a listener on the Spinner to handle filter selection and hide the Spinner after selection.
         * This section listens for user interaction with the Spinner. When an item is selected, the Spinner
         * is immediately hidden to prevent the selected text from being displayed permanently in the layout.
         */
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();

                // Skip the dummy "Select filter"
                if (position == 0) return;

                // Always reset selection back to 0 so this triggers next time
                filterSpinner.setSelection(0, false);

                // Hide the Spinner right after a selection
                filterSpinner.setVisibility(View.GONE);

                if (selectedFilter.equals("Filter by text")) {
                    filterSpinner.setSelection(0, false);

                    // Create a simple input dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Enter search phrase");

                    final EditText input = new EditText(HomeActivity.this);
                    builder.setView(input);

                    builder.setPositiveButton("Filter", (dialog, which) -> {
                        String keyword = input.getText().toString().trim().toLowerCase();
                        filterMyPostsByReason(keyword);
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set tab listeners (do this ONCE, not inside switchTab!)
        tabExplore.setOnClickListener(v -> switchTab(exploreEvents, tabExplore));
        tabFollowed.setOnClickListener(v -> switchTab(followedEvents, tabFollowed));
        tabMyPosts.setOnClickListener(v -> switchTab(myPostsEvents, tabMyPosts));
        tabMap.setOnClickListener(v -> {
            switchTab(new ArrayList<>(), tabMap);
            startActivity(new Intent(HomeActivity.this, MoodMap.class));
        });

        // Add new event
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MoodCreateAndEditActivity.class)));

        // Profile and notification buttons
        CardView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        ImageButton notificationButton = findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        // Load events and handle any incoming event
        loadAllEvents(this::handleIncomingEvent);
        //handleIncomingEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //loadAllEvents();
        loadAllEvents(this::handleIncomingEvent);
    }

    /**
     * Handle incoming event to add or update.
     */
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
            // DEFAULT TO EXPLORE TAB IF NOTHING SELECTED
            switchTab(exploreEvents, tabExplore);
        }
    }

    /**
     * Load events from Firestore and update UI.
     */
    private void loadAllEvents(Runnable afterLoad) {
        User currentUser = User.getInstance();
        database.loadEventsFromFirebase(currentUser, (myPosts, explore, followed) -> {
            if (myPosts != null && explore != null && followed != null) {
                myPostsEvents.clear();
                exploreEvents.clear();
                followedEvents.clear();

                myPostsEvents.addAll(myPosts);
                exploreEvents.addAll(explore);
                followedEvents.addAll(followed);

                adapter.notifyDataSetChanged();

                if (afterLoad != null) {
                    afterLoad.run(); // Run your logic after everything is loaded
                }
            } else {
                Log.w("HomeActivity", "Failed to load events from Firestore");
            }
        });
    }

    /**
     * Switch tabs and update UI.
     */
    private void switchTab(List<Event> targetList, TextView selectedTabView) {
        // Update current tab tracking
        currentTab = selectedTabView.getText().toString().toLowerCase();

        // Reset styles for all tabs
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Highlight selected tab
        selectedTabView.setTypeface(null, android.graphics.Typeface.BOLD);

        // Respect filter only if we are on myposts tab
        if ("myposts".equals(currentTab) && currentTextFilter != null && !currentTextFilter.isEmpty()) {
            filterMyPostsByReason(currentTextFilter); // Filtered view
        } else {
            adapter.setEvents(targetList); // Unfiltered view
        }
    }


    private void filterMyPostsByReason(String keyword) {
        if (keyword.isEmpty()) {
            switchTab(myPostsEvents, tabMyPosts); // Reset
            return;
        }

        String lowerKeyword = keyword.toLowerCase();
        List<Event> exactMatches = new ArrayList<>();
        List<Event> partialMatches = new ArrayList<>();

        for (Event e : myPostsEvents) {
            String reason = e.getMoodExplanation() != null ? e.getMoodExplanation().toLowerCase() : "";

            // Match exact word (surrounded by word boundaries)
            if (reason.matches(".*\\b" + Pattern.quote(lowerKeyword) + "\\b.*")) {
                exactMatches.add(e);
            } else if (reason.contains(lowerKeyword)) {
                partialMatches.add(e);
            }
        }

        // Sort both lists by date descending
        Comparator<Event> dateDescComparator = (e1, e2) -> e2.getDate().compareTo(e1.getDate());
        exactMatches.sort(dateDescComparator);
        partialMatches.sort(dateDescComparator);

        // Combine results: exact matches first
        List<Event> filteredList = new ArrayList<>(exactMatches);
        filteredList.addAll(partialMatches);

        adapter.setEvents(filteredList);
    }


    /**
     * Saves a new event to Firestore under the "MyPosts" collection.
     */
    private void saveEventToFirebase(Event event) {
        database.saveEventToFirebase(event, success -> {
            if (success) {
                Log.d("HomeActivity", "Event saved successfully");
            } else {
                Log.e("HomeActivity", "Error saving event");
            }
        });
    }

    /**
     * Update event in Firestore using Database.java
     */
    private void updateEventInFirebase(Event event) {
        database.updateEvent(event, success -> {
            if (success) {
                Log.d("HomeActivity", "Event updated successfully");
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("HomeActivity", "Error updating event");
                Toast.makeText(this, "Failed to update event!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

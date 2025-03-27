package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Color;
import android.health.connect.LocalTimeRangeFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.type.DateTime;

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
    private ImageButton filterButton;

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
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("AuthDebug", "No user session found! Redirecting to login.");
            Intent loginIntent = new Intent(this, LoginPageActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish(); // Stop execution of HomeActivity if user is not logged in
        } else {
            Log.d("AuthDebug", "User logged in: " + currentUser.getEmail());
        }
    }

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
        filterButton = findViewById(R.id.menu_button);
        //Spinner filterSpinner = findViewById(R.id.filter_spinner);


        // set up popupMenu for both followed and myposts tab
        filterButton.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.filter_popup_menu, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popupWindow.showAsDropDown(filterButton);

            Button recentWeekButton = popupView.findViewById(R.id.recent_week_option);
            Button emotionalStateButton = popupView.findViewById(R.id.emotional_state_option);
            Button filterTextButton = popupView.findViewById(R.id.text_filter_option);
            Button clearFilter = popupView.findViewById(R.id.clear_filter_option);
            Button threeRecentPost = popupView.findViewById(R.id.three_recent_option);

            if ("myposts".equals(currentTab)) {
                // Show multiple options
                threeRecentPost.setVisibility(View.GONE);
            } else {
                threeRecentPost.setOnClickListener(view -> {
                    filterRecentThree();
                });
            }

            recentWeekButton.setOnClickListener(view -> {
                if ("followed".equals(currentTab)){
                    try {
                        recentWeek(followedEvents);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        recentWeek(myPostsEvents);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                popupWindow.dismiss();
            });

            emotionalStateButton.setOnClickListener(view -> {
                showEmotionFilterDialog();
                popupWindow.dismiss();
            });

            filterTextButton.setOnClickListener(view -> {
                showFilterTextDialog();
                popupWindow.dismiss();
            });

            clearFilter.setOnClickListener(view -> {
                currentTextFilter = null;
                if ("myposts".equals(currentTab)) {
                    adapter.setEvents(myPostsEvents);
                } else if ("followed".equals(currentTab)) {
                    adapter.setEvents(followedEvents);
                } else if ("explore".equals(currentTab)) {
                    adapter.setEvents(exploreEvents);
                }
                popupWindow.dismiss();
            });


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
        ActivityResultLauncher<Intent> notificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        setIntent(data); // Retain selectedTab and textFilter for handleIncomingEvent()
                        handleIncomingEvent(); // Reapply tab and filter
                    }
                }
        );

        notificationButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            intent.putExtra("selectedTab", currentTab);
            intent.putExtra("textFilter", currentTextFilter);
            notificationLauncher.launch(intent);
        });


        // Load events and handle any incoming event
        loadAllEvents(this::handleIncomingEvent);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        String deletedEventId = intent.getStringExtra("deletedEventId");
        if (deletedEventId != null) {
            myPostsEvents.removeIf(event -> event.getID().equals(deletedEventId));
        }
    }

    /**
     * Load events from Firestore and update UI.
     * And edited to force sort
     */
    private void loadAllEvents(Runnable afterLoad) {
        User currentUser = User.getInstance();

        if (currentUser == null) {
            Log.e("UserDebug", "User instance is NULL! Skipping event loading.");
            return; // Prevent crash
        }

        database.loadEventsFromFirebase(currentUser, (myPosts, explore, followed) -> {
            if (myPosts != null && explore != null && followed != null) {
                myPostsEvents.clear();
                exploreEvents.clear();
                followedEvents.clear();

                // 2. Sort each list by timestamp descending
                myPosts.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                explore.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                followed.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));

                // 3. Add them to your local lists
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
        currentTab = selectedTabView.getText().toString().toLowerCase();

        // Reset styles for all tabs
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Highlight selected tab
        selectedTabView.setTypeface(null, android.graphics.Typeface.BOLD);

        // Enable/disable filter button
        if ("explore".equals(currentTab)) {
            filterButton.setEnabled(false);
            filterButton.setAlpha(0.3f); // Visually indicate it's disabled
        } else {
            filterButton.setEnabled(true);
            filterButton.setAlpha(1f); // Full opacity
        }

        // Apply filter if applicable
        if (("myposts".equals(currentTab) || "followed".equals(currentTab)) &&
                currentTextFilter != null && !currentTextFilter.isEmpty()) {
            filterEventsByReason(currentTextFilter);
        } else {
            adapter.setEvents(targetList);
        }
    }

    private void showFilterTextDialog(){
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


        // Sort both lists by date descending
        Comparator<Event> dateDescComparator = (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp());

        exactMatches.sort(dateDescComparator);
        partialMatches.sort(dateDescComparator);

        List<Event> filteredList = new ArrayList<>(exactMatches);
        filteredList.addAll(partialMatches);

        adapter.setEvents(filteredList);
    }

    /**
     * Saves a new event to Firestore under the "MyPosts" collection.
     * @param event event to be saved to the firebase
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
     * @param event event to be updated on the firebase
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

    /**
     * Get recent week of mood for either my mood events or the people that the user followed's posts
     * set the adapter to the new recent week mood list
     * @param posts  list of posts (myPosts/followedPosts) to find recent week posts
     * @return recentWeekEvents list of posts from last 7 days sorted desc
     */
    private List<Event> recentWeek(List<Event> posts) throws ParseException {
        /*Stackoverflow:
        https://stackoverflow.com/questions/16982056/how-to-get-the-date-7-days-earlier-date-from-current-date-in-java
         */

        List<Event> recentWeekEvents = new ArrayList<>();

        // get Calendar instance
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Date currentDate = cal.getTime();
        // substract 7 days
        // If we give 7 there it will give 8 days back
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-6);
        // convert to date
        Date recentWeekDate = cal.getTime();

        for (Event e: posts) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");
            Date eventDate = simpleDateFormat.parse(e.getDate());

            // if event date is not before the recent week and after current date
            if (!eventDate.before(recentWeekDate) && !eventDate.after(currentDate)) {
                recentWeekEvents.add(e);
            }
        }

        // Sort both lists by date descending
        Comparator<Event> dateDescComparator = (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp());
        recentWeekEvents.sort(dateDescComparator);

        adapter.setEvents(recentWeekEvents);
        return recentWeekEvents;
    }

    // --- New Methods for Emotional State Filtering ---

    /**
     * Displays a multi-choice dialog for filtering posts by emotional state.
     */
    private void showEmotionFilterDialog() {
        // Retrieve the emotional state options from strings.xml
        final String[] moodOptions = getResources().getStringArray(R.array.emotional_state_list);
        final boolean[] selectedMoods = new boolean[moodOptions.length];


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(getString(R.string.select_emotion_prompt)); // uses "Select Emotion Prompt" from strings.xml

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
            } else if ("followed".equals(currentTab)){
                filterPostsByMood(chosenMoods, followedEvents);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }

    /**
     * Filters the posts list based on the selected emotional states.
     * @param chosenMoods List of selected moods.
     * @param allPosts list of all posts in the current tab
     */
    private void filterPostsByMood(List<String> chosenMoods, List<Event> allPosts) {
        if (chosenMoods.isEmpty()) {
            if (allPosts == myPostsEvents) {
                switchTab(myPostsEvents, tabMyPosts);
            } else if (allPosts == followedEvents){
                switchTab(followedEvents, tabFollowed);
            }
            return;
        }

        List<Event> filteredList = new ArrayList<>();
        for (Event event : allPosts) {
            // Ensure the event's mood matches one of the chosen moods
            if (chosenMoods.contains(event.getMood())) {
                filteredList.add(event);
            }
        }

        // Sort filtered events by timestamp descending
        filteredList.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));

        adapter.setEvents(filteredList);
        if (allPosts == myPostsEvents){
            switchTab(filteredList, tabMyPosts);
        } else if (allPosts == followedEvents) {
            switchTab(filteredList, tabFollowed);
        }
    }

    private void filterRecentThree(){
        List<Event> followedPosts = followedEvents;
        List<Event> recentThree = new ArrayList<Event>();
        for (int i = 0; i < 3; i++){
            recentThree.add(followedPosts.get(i));
        }
        adapter.setEvents(recentThree);
    }


}
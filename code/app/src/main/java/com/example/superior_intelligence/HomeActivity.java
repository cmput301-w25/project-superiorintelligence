/**
 * HomeActivity class for managing the main home screen of the application.
 * This activity handles displaying events, managing tabs (Explore, Followed, MyPosts),
 * and handling user interactions such as adding new events, switching tabs, and following/unfollowing events.
 *
 * Methods:
 *
 * - onCreate(Bundle savedInstanceState)
 *   Initializes the activity, sets up RecyclerView, tabs, and loads data from Firestore.
 *
 * - onResume()
 *   Refreshes the RecyclerView data when the activity resumes.
 *
 * - saveEventToFirebase(Event event)
 *   Saves an event to Firestore under the "MyPosts" collection.
 *   - @param event The event object to be saved.
 *
 * - loadEventsFromFirebase()
 *   Loads events from Firestore into the MyPosts list and updates the RecyclerView.
 *
 * - switchTab(List<Event> targetList, TextView selectedTab)
 *   Switches between tabs by updating the RecyclerView with the target list and highlighting the selected tab.
 *   - @param targetList The list of events to display in the RecyclerView.
 *   - @param selectedTab The tab to be highlighted.
 *
 * - onFollowToggled(Event event, boolean isFollowed)
 *   Called when the user toggles the follow/unfollow state of an event in the adapter.
 *   - @param event The event whose follow state was toggled.
 *   - @param isFollowed The new follow state of the event.
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.superior_intelligence.Database;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import android.util.Log;

/**
 *
 */
public class HomeActivity extends AppCompatActivity implements EventAdapter.OnFollowToggleListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private Database database;

    // Separate lists for each tab
    private List<Event> exploreEvents = new ArrayList<>();
    private List<Event> followedEvents = new ArrayList<>();
    private List<Event> myPostsEvents = new ArrayList<>();

    private TextView tabExplore, tabFollowed, tabMyPosts, tabMap;
    
    /**
     * Initializes the activity, sets up Firestore, event lists, and UI elements.
     * @param savedInstanceState instance of activity on start up
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        Log.d("HomeActivity", "HomeActivity started");

        database = new Database();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tab UI elements
        tabExplore = findViewById(R.id.tab_explore);
        tabFollowed = findViewById(R.id.tab_followed);
        tabMyPosts = findViewById(R.id.tab_myposts);
        tabMap = findViewById(R.id.tab_map);

        adapter = new EventAdapter(exploreEvents, followedEvents, myPostsEvents, this);
        recyclerView.setAdapter(adapter);

        loadAllEvents();

        Event newEvent = (Event) getIntent().getSerializableExtra("newEvent");
        if (newEvent != null) {
            // If it belongs to the current user
            if (newEvent.isMyPost() && !myPostsEvents.contains(newEvent)) {
                myPostsEvents.add(newEvent);
                adapter.setEvents(myPostsEvents);
                saveEventToFirebase(newEvent);
            }
            else if (!newEvent.isMyPost()) {
                exploreEvents.add(newEvent);
            }
            adapter.notifyDataSetChanged();
        }

        String selectedTab = getIntent().getStringExtra("selectedTab");
        if ("myposts".equals(selectedTab)) {
            switchTab(myPostsEvents, tabMyPosts);
        } else {
            switchTab(exploreEvents, tabExplore); // Ensure only Explore is bolded at start
        }

        // Set tab listeners
        tabExplore.setOnClickListener(v -> switchTab(exploreEvents, tabExplore));
        tabFollowed.setOnClickListener(v -> switchTab(followedEvents, tabFollowed));
        tabMyPosts.setOnClickListener(v -> switchTab(myPostsEvents, tabMyPosts));

        tabMap.setOnClickListener(v -> {
            switchTab(new ArrayList<>(), tabMap);
            startActivity(new Intent(HomeActivity.this, MoodMap.class));
        });

        // Launch's MoodCreateAndEditActivity when clicked
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodCreateAndEditActivity.class);
            startActivity(intent);
        });

        // Launch's ProfileActivity when clicked
        CardView profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Makes sure the RecyclerView updates when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); // Ensure RecyclerView updates when returning
    }

    /**
     * Saves a new event to Firestore under the "MyPosts" collection.
     * @param event The event object to be stored in Firestore.
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
     * Loads events from Firestore under the "MyPosts" collection and updates the UI.
     */
    private void loadAllEvents() {
        User currentUser = User.getInstance();
        database.loadEventsFromFirebase(currentUser, (myPosts, explore, followed) -> {
            if (myPosts != null && explore != null && followed != null) {
                // Replace local lists
                myPostsEvents.clear();
                exploreEvents.clear();
                followedEvents.clear();

                myPostsEvents.addAll(myPosts);
                exploreEvents.addAll(explore);
                followedEvents.addAll(followed);

                // Decide which tab to display
                String selectedTab = getIntent().getStringExtra("selectedTab");
                if ("myposts".equals(selectedTab)) {
                    switchTab(myPostsEvents, tabMyPosts);
                } else if ("followed".equals(selectedTab)) {
                    switchTab(followedEvents, tabFollowed);
                } else {
                    switchTab(exploreEvents, tabExplore);
                }

                adapter.notifyDataSetChanged();
            } else {
                // Handle load error
                Log.w("HomeActivity", "Failed to load events from Firestore");
            }
        });
    }


    /**
     * Switches between Explore, Followed, and MyPosts tabs, updating the UI accordingly.
     *
     * @param targetList  The list of events to display.
     * @param selectedTab The selected tab's TextView to apply bold styling.
     */
    private void switchTab(List<Event> targetList, TextView selectedTab) {
        // Reset all tabs to normal style
        tabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFollowed.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMyPosts.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);
        // Bold only the selected tab
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);

        // If the selected tab is not Mood Map, update the RecyclerView
        if (selectedTab != tabMap) {
            adapter.setEvents(targetList);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        };

    }

    /**
     * Handles follow/unfollow toggle events.
     *
     * @param event     The event object being followed/unfollowed.
     * @param isFollowed True if the event is now followed, false otherwise.
     */
    @Override
    public void onFollowToggled(Event event, boolean isFollowed) {
        // Will be helpful for whoever works on logic (e.g., Toasts, DB saving).

    }
}

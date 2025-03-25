package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<String> notificationsList = new ArrayList<>();
    private boolean isViewingIncomingRequests = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications); // Loads notifications.xml

        recyclerView = findViewById(R.id.notifications_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchIncomingRequests();

        ImageButton backButton = findViewById(R.id.notifications_back_button);
        backButton.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedTab", getIntent().getStringExtra("selectedTab"));
            resultIntent.putExtra("textFilter", getIntent().getStringExtra("textFilter"));
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Handle Tab Switching
        TabLayout tabLayout = findViewById(R.id.notifications_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    isViewingIncomingRequests = true;
                    fetchIncomingRequests();
                } else {
                    isViewingIncomingRequests = false;
                    fetchPendingRequests();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Fetches pending follow requests (requests where the user is the one requesting).
     */
    private void fetchPendingRequests() {
        User user = User.getInstance();
        Userbase.getInstance().getPendingFollowRequests(user.getUsername(), requests -> {
            notificationsList.clear();
            notificationsList.addAll(requests);
            adapter = new NotificationAdapter(notificationsList, true);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Fetches incoming follow requests from Firestore.
     */
    private void fetchIncomingRequests() {
        User user = User.getInstance();

        Userbase.getInstance().getIncomingFollowRequests(user.getUsername(), requests -> {
            if (requests != null) {
                notificationsList.clear();
                notificationsList.addAll(requests);
                adapter = new NotificationAdapter(notificationsList, false);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }
}

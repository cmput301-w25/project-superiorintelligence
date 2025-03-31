/**
 * completes story: us 03.02.01
 * see link: https://github.com/orgs/cmput301-w25/projects/9?pane=issue&itemId=102361986&issue=cmput301-w25%7Cproject-superiorintelligence%7C197
 * user is able to search up other users based on their username which is stored in firestore
 */

package com.example.superior_intelligence;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserHelper> userList;
    private FirebaseFirestore db;
    private EditText searchEditText;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_users);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get current user
        currentUsername = User.getInstance().getUsername();

        // Initialize UI components
        searchEditText = findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.recyclerView); // Add this to your layout
        AppCompatButton searchButton = findViewById(R.id.user_search_button);
        AppCompatButton clearButton = findViewById(R.id.clear_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, foundUser -> {
            Intent intent = new Intent(SearchUsersActivity.this, OtherUserProfileActivity.class);
            intent.putExtra("username", foundUser.getUsername());
            startActivity(intent);
        });

        recyclerView.setAdapter(userAdapter);

        // Back button to returns to HomeActivity
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Closes the current activity and returns to the previous screen
            }
        });

        // Load all users initially
        loadAllUsers();

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                searchUsers(searchQuery);
            } else {
                loadAllUsers();
            }
        });

        // Clear button click listener
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            loadAllUsers();
        });
    }

    private void loadAllUsers() {
        userList.clear();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserHelper user = document.toObject(UserHelper.class);
                            if (!user.getUsername().equals(currentUsername)) {
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                });
    }

    //the commented out code displays only the user with the exact same username as searched
    // the one below shows all users with the given letter sequence

//    private void searchUsers(String username) {
//        userList.clear();
//        db.collection("users")
//                .whereEqualTo("username", username)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            HelperClass user = document.toObject(HelperClass.class);
//                            if (!user.getUsername().equals(currentUsername)) {
//                                userList.add(user);
//                            }
//                        }
//                        userAdapter.notifyDataSetChanged();
//                    }
//                });
//    }

    // search users based on username,
    // implemented so it will pick all users that start with the given letter search
    // so for example ba will find barbs, but ar will not
    private void searchUsers(String searchQuery) {
        userList.clear();
        // Use orderBy with startAt and endAt for prefix matching
        db.collection("users")
                .orderBy("username")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff") // \uf8ff is a high Unicode character to match all strings starting with searchQuery
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserHelper user = document.toObject(UserHelper.class);
                            if (!user.getUsername().equals(currentUsername)) {
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                });
    }

}
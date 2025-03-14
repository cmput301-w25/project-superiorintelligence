package com.example.superior_intelligence;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications); // Loads notifications.xml

        // Find the back button and set a click listener
        ImageButton backButton = findViewById(R.id.notifications_back_button);
        backButton.setOnClickListener(view -> finish()); // Closes NotificationActivity and returns to HomeActivity
    }
}

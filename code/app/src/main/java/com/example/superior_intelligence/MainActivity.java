/**
 * completes sub issue: Create app main page. #130
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=99976897&issue=cmput301-w25%7Cproject-superiorintelligence%7C130
 * Connects to app_main_page.xml
 * Functionality: contains a login/sign up button that brings you to LoginPageActivity.java
 * further functionaly may be given at a later time
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_page);

        AppCompatButton loginButton = findViewById(R.id.login_button_login_page);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginPageActivity.class);
                startActivity(intent);
            }
        });
    }
}
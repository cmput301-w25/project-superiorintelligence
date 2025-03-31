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

/**
 * The main entry point activity for the application.
 * This activity serves as the launch screen and provides navigation to the login page.
 * Currently contains a single button that redirects to the LoginPageActivity
 */
public class MainActivity extends AppCompatActivity{

    /**
     * Initializes the activity and sets up the main layout.
     * Sets up the click listener for the login button which navigates to
     * LoginPageActivity when pressed.
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most recently supplied.
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_page);

        AppCompatButton loginButton = findViewById(R.id.login_button_login_page);
        loginButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the login button click event.
             * Starts the LoginPageActivity when the button is clicked.
             * @param v The view that was clicked (the login button)
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginPageActivity.class);
                startActivity(intent);
            }
        });
    }
}
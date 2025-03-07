/**
 * completes sub-issue: CreateAccountPage #144
 * link: https://github.com/orgs/cmput301-w25/projects/9/views/1?pane=issue&itemId=100541362&issue=cmput301-w25%7Cproject-superiorintelligence%7C144
 * Creates an account by entering a unique username. Checks database to ensure username is unique and doesn't exist
 */

package com.example.superior_intelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.superior_intelligence.HomePageActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    EditText signupName, signupUsername;
    //TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_page);

        signupName = findViewById(R.id.signup_name);
        signupUsername = findViewById(R.id.signup_username);
        //loginRedirectText = findViewById(R.id.loginRedirectText);

        // Set click listener to navigate back
        AppCompatButton SignupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String name = signupName.getText().toString();
                String username = signupUsername.getText().toString();

                HelperClass helperClass = new HelperClass(name, username);
                reference.child(username).setValue(helperClass);

                Toast.makeText(CreateAccountActivity.this, "Your signup is successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateAccountActivity.this, LoginPageActivity.class);
                startActivity(intent);
            }
        });

//        loginRedirectText.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                Intent intent = new Intent(CreateAccountActivity.this, LoginPageActivity.class);
//                startActivity(intent);
//            }
//        });


        // Find the back button
        ImageButton backButton = findViewById(R.id.back_button);

        // Set click listener to navigate back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Closes the current activity and returns to MainActivity
            }
        });

//        AppCompatButton SignupButton = findViewById(R.id.signup_button);
//        SignupButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(CreateAccountActivity.this, HomePageActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}

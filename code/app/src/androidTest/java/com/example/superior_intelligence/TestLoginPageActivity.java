package com.example.superior_intelligence;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestLoginPageActivity {

    @Before
    public void launchActivity() {
        ActivityScenario.launch(LoginPageActivity.class);
    }

    @Test
    public void validateUsername_showsErrorWhenEmpty() {
        onView(withId(R.id.login_username)).perform(clearText());
        onView(withId(R.id.login_password)).perform(typeText("password123"));
        onView(withId(R.id.login_button)).perform(click());

        onView(withId(R.id.login_username))
                .check(matches(withErrorText("Username cannot be empty")));
    }

    @Test
    public void validatePassword_showsErrorWhenEmpty() {
        onView(withId(R.id.login_username)).perform(typeText("testuser"));
        onView(withId(R.id.login_password)).perform(clearText());
        onView(withId(R.id.login_button)).perform(click());

        onView(withId(R.id.login_password))
                .check(matches(withErrorText("Password cannot be empty")));
    }

    @Test
    public void validateFields_noErrorWhenBothValid() {
        onView(withId(R.id.login_username)).perform(typeText("testuser"));
        onView(withId(R.id.login_password)).perform(typeText("mypassword"));
        onView(withId(R.id.login_button)).perform(click());

        // If no error and login proceeds, test passes. Optional: add intent check.
    }

    // Custom matcher to check EditText error messages
    public static Matcher<View> withErrorText(final String expectedError) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof EditText)) return false;
                CharSequence error = ((EditText) view).getError();
                return error != null && error.toString().equals(expectedError);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with error: " + expectedError);
            }
        };
    }
}

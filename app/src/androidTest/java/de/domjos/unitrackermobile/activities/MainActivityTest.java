package de.domjos.unitrackermobile.activities;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.domjos.unitrackermobile.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4ClassRunner.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testDialogOnStartUp() {
        boolean first = false;
        try {
            onView(withText(R.string.pwd_title)).check(matches(isDisplayed()));
            first = true;
        } catch (Exception ex) {
            onView(withText(R.string.pwd_title_pwd)).check(matches(isDisplayed()));
        }
        onView(withHint(R.string.pwd_password1)).check(matches(isDisplayed()));

        if(first) {
            onView(withHint(R.string.pwd_password2)).check(matches(isDisplayed()));
        } else {
            onView(withHint(R.string.pwd_password2)).check(matches(not(isDisplayed())));
        }
    }

}

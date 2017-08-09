package com.fesskiev.mediacenter.testing;


import android.Manifest;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.splash.SplashActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class NavigateAppTest {

    @Rule
    public ActivityTestRule<SplashActivity> activityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Test
    public void navigationAppEspressoTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         *  START WALKTHROUGH
         */

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonGranted), withText("YES"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatButton.perform(click());

        PermissionGranter.allowPermissionsIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionGranter.allowPermissionsIfNeeded(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        PermissionGranter.allowPermissionsIfNeeded(Manifest.permission.RECORD_AUDIO);


        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.fetchMediaButton), withText("YES"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatButton2.perform(click());

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.viewpager)).perform(swipeLeft());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.enterAppButton), withText("Enter"),
                        childAtPosition(
                                allOf(withId(R.id.walkRoot),
                                        childAtPosition(
                                                withId(R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());


        /**
         *  FINISH WALKTHROUGH
         */

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction frameLayoutWithHole = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.content),
                                childAtPosition(
                                        withId(R.id.action_bar_root),
                                        1)),
                        1),
                        isDisplayed()));
        frameLayoutWithHole.perform(click());

        ViewInteraction frameLayoutWithHole2 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.content),
                                childAtPosition(
                                        withId(R.id.action_bar_root),
                                        1)),
                        1),
                        isDisplayed()));
        frameLayoutWithHole2.perform(click());

        ViewInteraction frameLayoutWithHole3 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.content),
                                childAtPosition(
                                        withId(R.id.action_bar_root),
                                        1)),
                        1),
                        isDisplayed()));
        frameLayoutWithHole3.perform(click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.imageHeader), withContentDescription("SoloPlayer Pro"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatImageView.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pressBack();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction playPauseButton = onView(
                allOf(withId(R.id.playPauseButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.audioControl),
                                        0),
                                1),
                        isDisplayed()));
        playPauseButton.perform(click());

        pressBack();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.nav_view_main),
                                        0)),
                        9),
                        isDisplayed()));
        navigationMenuItemView.perform(click());


        pressBack();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction navigationMenuItemView2 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.nav_view_main),
                                        0)),
                        3),
                        isDisplayed()));
        navigationMenuItemView2.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pressBack();


        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonExitConfirm), withText("YES"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatButton4.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

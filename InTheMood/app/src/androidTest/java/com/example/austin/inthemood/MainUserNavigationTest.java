package com.example.austin.inthemood;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by annaholowaychuk on 2017-03-13.
 */

public class MainUserNavigationTest extends ActivityInstrumentationTestCase2<MainUser> {
    private Solo solo;
    public MainUserNavigationTest() {
        super(com.example.austin.inthemood.MainUser.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testStart() throws Exception {
        Activity activity = getActivity();
    }

    public void testNavigation() {
        solo.assertCurrentActivity("Wrong Activity", MainUser.class);



        solo.clickOnButton("My Moods");
        solo.assertCurrentActivity("Wrong Activity", MyMoods.class);
        solo.goBack();
        solo.assertCurrentActivity("Wrong Activity", MainUser.class);

        solo.clickOnButton("My Friends");
        solo.assertCurrentActivity("Wrong Activity", MyFriends.class);
        solo.goBack();
        solo.assertCurrentActivity("Wrong Activity", MainUser.class);

        solo.clickOnButton("Nearby Moods");
        solo.assertCurrentActivity("Wrong Activity", MapActivity.class);
        solo.goBack();
        solo.assertCurrentActivity("Wrong Activity", MainUser.class);

        solo.clickOnButton("Mood Calendar");
        solo.assertCurrentActivity("Wrong Activity", MoodCalendarActivity.class);
        solo.goBack();
        solo.assertCurrentActivity("Wrong Activity", MainUser.class);




    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}

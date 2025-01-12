/*
 * MIT License
 *
 * Copyright (c) 2024 RUB-SE-LAB-2024
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.msbattery.batterymonitoringapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;

import android.Manifest;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import de.msbattery.batterymonitoringapp.helpers.ClickAction;
import de.msbattery.batterymonitoringapp.model.CompactModel;
import de.msbattery.batterymonitoringapp.model.CompactViewModel;
import de.msbattery.batterymonitoringapp.model.DetailedModel;
import de.msbattery.batterymonitoringapp.model.DetailedViewModel;
import de.msbattery.batterymonitoringapp.model.SimpleModel;
import de.msbattery.batterymonitoringapp.model.SimpleViewModel;
import de.msbattery.batterymonitoringapp.structures.Status;
import de.msbattery.batterymonitoringapp.views.SimpleView;

//all tests that test features of detailed view
@RunWith(MockitoJUnitRunner.class)
//@RunWith(AndroidJUnit4.class)
@LargeTest
public class DetailedViewTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionSMSSend = GrantPermissionRule.grant(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET);

    private MutableLiveData<SimpleModel> liveDataSimple;
    private SimpleViewModel mockViewModelSimple;

    private MutableLiveData<CompactModel> liveDataCompact;
    private CompactViewModel mockViewModelCompact;

    private MutableLiveData<DetailedModel> liveDataDetailed;
    private DetailedViewModel mockViewModelDetailed;

//    private SimpleIdlingResource idlingResource;

    @Before
    public void setup() {
        liveDataSimple = new MutableLiveData<>();
        mockViewModelSimple = Mockito.mock(SimpleViewModel.class);
        Mockito.when(mockViewModelSimple.getData()).thenReturn(liveDataSimple);

        ActivityScenario<MainActivity> scenario = activityRule.getScenario();
        scenario.onActivity(activity -> {
            SimpleView fragment = new SimpleView();
            fragment.setSimpleViewModel(mockViewModelSimple);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();
        });

        liveDataCompact = new MutableLiveData<>();
        mockViewModelCompact = Mockito.mock(CompactViewModel.class);
        Mockito.when(mockViewModelCompact.getData()).thenReturn(liveDataCompact);

        liveDataDetailed = new MutableLiveData<>();
        mockViewModelDetailed = Mockito.mock(DetailedViewModel.class);
        Mockito.when(mockViewModelDetailed.getData()).thenReturn(liveDataDetailed);

        //switch to detailed view already
        SimpleModel model = new SimpleModel();
        model.setStatus(Status.CRITICAL);
        model.setErrorMessages("CRITICAL");
        liveDataSimple.postValue(model);
        onView(withId(R.id.submit_button)).perform(click());

        onView(withId(R.id.switcher)).perform(click());
    }

    //tries to logout via button in detailed view
    @Test
    public void testLogoutFromDetailed() {
        onView(withId(R.id.logout)).perform(click());
        onView(withId(R.id.simple_view)).check(matches(isDisplayed()));
    }

    //tests decreasing the segment number in the detailed view
    @Test
    public void testDecreaseInDetailedView() {
        onView(withId(R.id.switcher)).perform(click()); //switch to compact view

        onView(withId(R.id.button2)).perform(click());

        onView(withId(R.id.detailedView)).check(matches(isDisplayed()));
        onView(withId(R.id.detailed_view_segment_number)).check(matches(withText("Segment 2")));

        ClickAction clickAction = ClickAction.clickAndVerify();
        onView(withId(R.id.decreaseSegmentButton)).perform(clickAction);
        assertTrue(clickAction.isClicked());

        onView(withId(R.id.detailed_view_segment_number)).check(matches(withText("Segment 1")));

        clickAction = ClickAction.clickAndVerify();
        onView(withId(R.id.decreaseSegmentButton)).perform(clickAction);
        assertTrue(clickAction.isClicked());

        //edge case: switch from 1 to 12
        onView(withId(R.id.detailed_view_segment_number)).check(matches(withText("Segment 12")));
    }

    //tests increasing the segment number in the detailed view
    @Test
    public void testIncreaseInDetailedView() {
        onView(withId(R.id.switcher)).perform(click()); //switch to compact view

        onView(withId(R.id.button11)).perform(click());

        onView(withId(R.id.detailedView)).check(matches(isDisplayed()));
        onView(withId(R.id.detailed_view_segment_number)).check(matches(withText("Segment 11")));

        ClickAction clickAction = ClickAction.clickAndVerify();
        onView(withId(R.id.increaseSegmentButton)).perform(clickAction);
        assertTrue(clickAction.isClicked());

        onView(withId(R.id.detailed_view_segment_number)).check(matches(withText("Segment 12")));

        clickAction = ClickAction.clickAndVerify();
        onView(withId(R.id.increaseSegmentButton)).perform(clickAction);
        assertTrue(clickAction.isClicked());

        //edge case: switch from 1 to 12
        onView(withId(R.id.detailed_view_segment_number)).check(matches(withText("Segment 1")));
    }

    //test  switch to settings via settings button
    @Test
    public void testSettingsButton() {
        onView(withId(R.id.settings)).perform(click());
        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));
    }

}

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
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.StringContains.containsString;

import android.Manifest;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import de.msbattery.batterymonitoringapp.model.CompactModel;
import de.msbattery.batterymonitoringapp.model.CompactViewModel;
import de.msbattery.batterymonitoringapp.model.SimpleModel;
import de.msbattery.batterymonitoringapp.model.SimpleViewModel;
import de.msbattery.batterymonitoringapp.structures.Status;
import de.msbattery.batterymonitoringapp.views.SimpleView;

@RunWith(MockitoJUnitRunner.class)
public class SettingsTests {

    private static final String correctPW = "1234";

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

        //Login and switch to settings view already
        SimpleModel model = new SimpleModel();
        model.setStatus(Status.CRITICAL);
        model.setErrorMessages("CRITICAL");
        liveDataSimple.postValue(model);

        onView(withId(R.id.submit_button)).perform(click());
        onView(withId(R.id.settings)).perform(click());
    }

    @Test
    public void testAddEmergencyContactAllInfo() {
        String name = "FirstName LastName";
        String email = "example@gmail.com";
        String phone = "1234567890";
        //add new contact
        onView(withId(R.id.edit_text_name)).perform(typeText(name), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_mail)).perform(typeText(email), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_phone)).perform(typeText(phone), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_add_contact)).perform(click());

        //assert that it is visible in list
        onView(withId(R.id.contact_name)).check(matches(withText(name)));
        onView(withId(R.id.contact_email)).check(matches(withText(email)));
        onView(withId(R.id.contact_phone)).check(matches(withText(phone)));

        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Yes")).perform(click());
    }

    @Test
    public void testLogoutFromSettings() {
        onView((withId(R.id.logout))).perform(click());
        onView(withId(R.id.simple_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddEmergencyContactName(){
        String name = "FirstName LastName";

        onView(withId(R.id.edit_text_name)).perform(typeText(name), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_add_contact)).perform(click());

        onView(withId(R.id.contact_name)).check(matches(withText(name)));

        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Yes")).perform(click());
    }

    @Test
    public void testAddEmergencyContactNamePhone(){
        String name = "FirstName LastName";
        String phone = "1234567890";

        onView(withId(R.id.edit_text_name)).perform(typeText(name), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_phone)).perform(typeText(phone), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_add_contact)).perform(click());

        onView(withId(R.id.contact_name)).check(matches(withText(name)));
        onView(withId(R.id.contact_phone)).check(matches(withText(phone)));

        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Yes")).perform(click());
    }

    @Test
    public void testAddEmergencyContactNameMail(){
        String name = "FirstName LastName";
        String email = "example@gmail.com";

        onView(withId(R.id.edit_text_name)).perform(typeText(name), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_mail)).perform(typeText(email), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_add_contact)).perform(click());

        onView(withId(R.id.contact_name)).check(matches(withText(name)));
        onView(withId(R.id.contact_email)).check(matches(withText(email)));

        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Yes")).perform(click());
    }

    @Test
    public void testAddEmergencyContactNoName() {
        String email = "example@gmail.com";
        String phone = "1234567890";

        //try to add new contact
        onView(withId(R.id.edit_text_mail)).perform(typeText(email), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_phone)).perform(typeText(phone));
        onView(withId(R.id.button_add_contact)).perform(click());

        //assert that this did not work
        onView(withId(R.id.contact_name)).check(doesNotExist());
    }

    @Test
    public void testAddToDisplayedContacts() {
        String name = "FirstName LastName";
        String email = "example@gmail.com";
        String phone = "1234567890";

        //add new contact
        onView(withId(R.id.edit_text_name)).perform(typeText(name), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_mail)).perform(typeText(email), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_phone)).perform(typeText(phone), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_add_contact)).perform(click());

        //add to displayed contacts
        onView(withId(R.id.show)).perform(click());

        //logout and see if listed
        onView(withId(R.id.logout)).perform(click());
        onView(withText(containsString(name))).check(matches(isDisplayed()));

        //cleanup
        onView(withId(R.id.password_text)).perform(typeText(correctPW), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.submit_button)).perform(click());
        onView(withId(R.id.settings)).perform(click());

        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Yes")).perform(click());


    }

    @Test
    public void testChangePassword() {
        String oldPw = correctPW;
        String newPw = "12345";

        //set password
        onView(withId(R.id.oldPassword)).perform(typeText(oldPw), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.newPassword)).perform(typeText(newPw), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_change_password)).perform(click());

        //logout
        onView(withId(R.id.logout)).perform(click());

        //try to login with new pw
        onView(withId(R.id.password_text)).perform(typeText(newPw), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.submit_button)).perform(click());

        //compact view shows if login was successfull
        onView(withId(R.id.compact_view)).check(matches(isDisplayed()));

        //cleanup
        onView(withId(R.id.settings)).perform(click());

        onView(withId(R.id.oldPassword)).perform(typeText(newPw), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.newPassword)).perform(typeText(oldPw), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_change_password)).perform(click());
    }

    @Test
    public void testChangePasswordFail() {
        String newPw = "1";

        onView(withId(R.id.newPassword)).perform(typeText(newPw), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.button_change_password)).perform(click());

        onView(withId(R.id.logout)).perform(click());
        onView(withId(R.id.password_text)).perform(typeText(newPw), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.compact_view)).check(doesNotExist());
    }
}

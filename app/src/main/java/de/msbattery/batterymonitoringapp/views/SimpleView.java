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

package de.msbattery.batterymonitoringapp.views;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;


import java.util.ArrayList;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.connection.AlertSender;
import de.msbattery.batterymonitoringapp.database.DataBaseHelper;
import de.msbattery.batterymonitoringapp.model.SimpleModel;
import de.msbattery.batterymonitoringapp.model.SimpleViewModel;
import de.msbattery.batterymonitoringapp.structures.EmergencyContact;
import de.msbattery.batterymonitoringapp.structures.Status;

public class SimpleView extends Fragment {

    private MediaPlayer mediaPlayer;

    private static boolean isEmergency = false;

    private EditText passwordText;
    private TextInputLayout passwordInputSymbols;
    private Button submitButton;

    private String appPassword = "3BAKA"; //This hardcoded password prevents null error?

    private SimpleViewModel simpleViewModel;
    private TextView statusTextView;
    private TextView adviceBox;
    private TextView emergencyContactsTitle;

    private ImageView connectedStatus;

    private ImageView batteryStatusImage;
    private boolean passwordProtected = true;


    private ObjectAnimator textBlinkAnimator;
    private ObjectAnimator imageBlinkAnimator;
    private ObjectAnimator emergencyContactsAnimator;

    private final int blinkingRateMs = 500;

    private void hide_password_prompt() {
        this.passwordText.setVisibility(View.GONE);
        this.passwordInputSymbols.setVisibility(View.GONE);
    }

    private void show_password_prompt() {
        this.passwordText.setVisibility(View.VISIBLE);
        this.passwordInputSymbols.setVisibility(View.VISIBLE);
    }

    private void startBlinkingAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(this.blinkingRateMs); // duration of one tick/blink rate in ms
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.start();

        if (view instanceof TextView) {
            if (view.getId() == R.id.status) {
                textBlinkAnimator = animator;
            } else if (view.getId() == R.id.Emergency_contacts_title) {
                emergencyContactsAnimator = animator;
            }
        } else if (view instanceof ImageView) {
            imageBlinkAnimator = animator;
        }
    }

    private void stopBlinkingAnimation(View view) {
        ObjectAnimator animator = null;
        if (view instanceof TextView) {
            if (view.getId() == R.id.status) {
                animator = textBlinkAnimator;
            } else if (view.getId() == R.id.Emergency_contacts_title) {
                animator = emergencyContactsAnimator;
            }
        } else if (view instanceof ImageView) {
            animator = imageBlinkAnimator;
        }

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        // Ensure the view has alpha of 1 since the animation gives it alpha in [0, 1]
        view.setAlpha(1f);
    }


    //Color OKAY and CRITICAL but not angled brackets, alternatively seperate into own textview
    private SpannableStringBuilder getColoredStatusString(String status) {
        String text = String.format("[%s]", status);
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);

        int start = text.indexOf(status);
        int end = start + status.length();
        if ("OKAY".equals(status)) {
            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if ("CRITICAL".equals(status)) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

    public SimpleView() {
        // Required empty public constructor
    }

    public static SimpleView newInstance() {
        return new SimpleView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_simple_view, container, false);

        // PERMISSIONS
        AlertSender alerter = AlertSender.getInstance(getContext());
        if(alerter != null)
            if(alerter.canSendSms(getContext())) {
                Activity SVactivty = getActivity();
                if(SVactivty != null) {
                    if (ContextCompat.checkSelfPermission(SVactivty, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SVactivty, new String[]{Manifest.permission.SEND_SMS}, 1);
                        //requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                    }
                    if (ContextCompat.checkSelfPermission(SVactivty, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SVactivty, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
                    }
                }

            }

        // END PERMISSIONS

        passwordText = view.findViewById(R.id.password_text);
        passwordInputSymbols = view.findViewById(R.id.Password_login);
        submitButton = view.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = passwordText.getText().toString();
                DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
                appPassword = dataBaseHelper.select_password();
                if ( ( enteredPassword.equals(appPassword) ) || (!passwordProtected) ) {
                    // Password is correct, navigate to CompactViewFragment
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, CompactView.newInstance())
                            .addToBackStack(null)
                            .commit();
                } else {
                    // Password is incorrect, show a toast message
                    Toast.makeText(getActivity(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.alarm_beep); //beeep

        if(simpleViewModel == null)
            simpleViewModel = new ViewModelProvider(this).get(SimpleViewModel.class);
        statusTextView = view.findViewById(R.id.status);
        statusTextView.setText(getColoredStatusString("OKAY")); // kind of a hack to immediately color okay in green
        batteryStatusImage = view.findViewById(R.id.imageView2);
        adviceBox = view.findViewById(R.id.advice_box);
        emergencyContactsTitle = view.findViewById(R.id.Emergency_contacts_title);
        connectedStatus = view.findViewById(R.id.connected_status);

        TextView contactBox = view.findViewById(R.id.Phone);
        /*Email: Teto.Kasane@rub.de
        \n<u>Phone Number: 3.14159265358</u>
        \n
        \nEmail: Burger.Torten@rub.de
        \n<u>Phone Number: 2.718281828459</u></string>*/

        // do not work with contacts
        ArrayList<EmergencyContact> contacts;
        // work with copy instead
        ArrayList<EmergencyContact> contactsCopy;

        SpannableStringBuilder fullBoxText = new SpannableStringBuilder();

        contacts = AlertSender.getEmergencyContacts(getContext());
        if (contacts != null) {
            contactsCopy = new ArrayList<>(contacts);
            for(EmergencyContact con : contactsCopy) {
                if(con.getIs_displayed()) {
                    int start = fullBoxText.length();

                    String firstLine = "\nName: " + con.getName() + "\n";
                    String secondLine = "E-Mail: "  + con.getEmail() + "\n";
                    String thirdLine = "Phone number: "  + con.getPhoneNumber();

                    fullBoxText.append(firstLine).append(secondLine).append(thirdLine);
                    int end = start + firstLine.length();
                    fullBoxText.setSpan(new UnderlineSpan(), start, end, 0);
                }
            }
        }

        contactBox.setText(fullBoxText);

        simpleViewModel.getData().observe(getViewLifecycleOwner(), new Observer<SimpleModel>() {
            @Override
            public void onChanged(SimpleModel simpleModel) {
                statusTextView.setText(getColoredStatusString(simpleModel.getStatusString()));

                if(simpleModel.isConnected())
                    connectedStatus.setImageResource(R.drawable.connected);
                else
                    connectedStatus.setImageResource(R.drawable.disconnected);
                passwordProtected = true; // reinstate password protection if state changes
                show_password_prompt();
                Status currentStatus = simpleModel.getStatus();

                //stop animations generally since we will start them in critical state anyway
                stopBlinkingAnimation(statusTextView); // clear animation in case of state change
                stopBlinkingAnimation(batteryStatusImage); // clear animation for the image in case of state change
                stopBlinkingAnimation(emergencyContactsTitle); // clear animation for the emergency contacts title in case of state change

                /*Play sound (for this we have to do some processing directly in the view unfortunately)
                also since we are already checking, might as well lift password protection*/
                if ( ( currentStatus == Status.CRITICAL ) && ( mediaPlayer != null ) ) {
                    mediaPlayer.start();
                    passwordProtected = false;
                    //hide password input field in case of critical status
                    hide_password_prompt();

                    //start animations in critical case
                    startBlinkingAnimation(statusTextView);
                    startBlinkingAnimation(batteryStatusImage);
                    startBlinkingAnimation(emergencyContactsTitle);

                    batteryStatusImage.setImageResource(R.drawable.warning_symbol);

                    if(!isEmergency) {
                        isEmergency = true;
                        AlertSender alerter = AlertSender.getInstance(getContext());
                        if(alerter != null) {
                            String message = simpleModel.getErrorMessages().replace("]", "").replace("[", " ");
                            alerter.notifyEmergency(getContext(), message);
                        }
                    }

                }
                else if (currentStatus == Status.GOOD){
                    isEmergency = false;
                    batteryStatusImage.setImageResource(R.drawable.battery_v2a);
                }

                //Update advice box/Info Field next to battery
                adviceBox.setText(String.format("%s", simpleModel.getInfoField()));
            }
        });
    }

    public void setSimpleViewModel(SimpleViewModel mockViewModel) {
        this.simpleViewModel = mockViewModel;
    }
}
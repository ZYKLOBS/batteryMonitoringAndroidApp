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

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Button;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.customElements.CustomStatusBar2LinesView;
import de.msbattery.batterymonitoringapp.model.CompactModel;
import de.msbattery.batterymonitoringapp.model.CompactViewModel;

public class CompactView extends Fragment {

    private final int segmentCount = 12;

    private ConstraintLayout[] constraintLayouts = new ConstraintLayout[segmentCount];
    private Button[] buttons = new Button[segmentCount];
    private CompactViewModel compactViewModel;
    private TextView[] tempTextList = new TextView[segmentCount];
    private TextView[] voltTextList = new TextView[segmentCount];
    private CustomStatusBar2LinesView[] tempBarList = new CustomStatusBar2LinesView[segmentCount];
    private CustomStatusBar2LinesView[] voltBarList = new CustomStatusBar2LinesView[segmentCount];


    private ImageView connectedStatus;

    private void replaceFragment(String frag) {
        Fragment newFragment;
        switch (frag) {
            case "D":
                newFragment = DetailedView.newInstance();
                break;
            case "Set":
                newFragment = SettingsFragment.newInstance(1, "C");
                break;
            default:
                newFragment = SimpleView.newInstance();
                break;
        }
        FragmentManager fragmentManager = getParentFragmentManager(); // or getChildFragmentManager() if nested
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, newFragment); // R.id.fragment_container is the ID of the container in the activity layout
        fragmentTransaction.addToBackStack(null); // Optional: add transaction to back stack
        fragmentTransaction.commit();
    }



    private ObjectAnimator[] animators = new ObjectAnimator[12];
    private LinearLayout errorTextBox;
    private ScrollView errorScrollView;
    private static final int BLINKING_RATE_MS = 500;
    public CompactView() {
        // Required empty public constructor
    }

    public static CompactView newInstance() {
        return new CompactView();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compact_view, container, false);

        // Initialize the buttons by finding them in the view
        buttons[0] = view.findViewById(R.id.button1);
        buttons[1] = view.findViewById(R.id.button2);
        buttons[2] = view.findViewById(R.id.button3);
        buttons[3] = view.findViewById(R.id.button4);
        buttons[4] = view.findViewById(R.id.button5);
        buttons[5] = view.findViewById(R.id.button6);
        buttons[6] = view.findViewById(R.id.button7);
        buttons[7] = view.findViewById(R.id.button8);
        buttons[8] = view.findViewById(R.id.button9);
        buttons[9] = view.findViewById(R.id.button10);
        buttons[10] = view.findViewById(R.id.button11);
        buttons[11] = view.findViewById(R.id.button12);

        // Initialize the constraintLayouts by finding them in the view, unfortunately I did not manage this dynamically for now
        constraintLayouts[0] = view.findViewById(R.id.constraintLayout1);
        constraintLayouts[1] = view.findViewById(R.id.constraintLayout2);
        constraintLayouts[2] = view.findViewById(R.id.constraintLayout3);
        constraintLayouts[3] = view.findViewById(R.id.constraintLayout4);
        constraintLayouts[4] = view.findViewById(R.id.constraintLayout5);
        constraintLayouts[5] = view.findViewById(R.id.constraintLayout6);
        constraintLayouts[6] = view.findViewById(R.id.constraintLayout7);
        constraintLayouts[7] = view.findViewById(R.id.constraintLayout8);
        constraintLayouts[8] = view.findViewById(R.id.constraintLayout9);
        constraintLayouts[9] = view.findViewById(R.id.constraintLayout10);
        constraintLayouts[10] = view.findViewById(R.id.constraintLayout11);
        constraintLayouts[11] = view.findViewById(R.id.constraintLayout12);

        tempTextList[0] = view.findViewById(R.id.temperature1);
        tempTextList[1] = view.findViewById(R.id.temperature2);
        tempTextList[2] = view.findViewById(R.id.temperature3);
        tempTextList[3] = view.findViewById(R.id.temperature4);
        tempTextList[4] = view.findViewById(R.id.temperature5);
        tempTextList[5] = view.findViewById(R.id.temperature6);
        tempTextList[6] = view.findViewById(R.id.temperature7);
        tempTextList[7] = view.findViewById(R.id.temperature8);
        tempTextList[8] = view.findViewById(R.id.temperature9);
        tempTextList[9] = view.findViewById(R.id.temperature10);
        tempTextList[10] = view.findViewById(R.id.temperature11);
        tempTextList[11] = view.findViewById(R.id.temperature12);

        tempBarList[0] = view.findViewById(R.id.tempBar1);
        tempBarList[1] = view.findViewById(R.id.tempBar2);
        tempBarList[2] = view.findViewById(R.id.tempBar3);
        tempBarList[3] = view.findViewById(R.id.tempBar4);
        tempBarList[4] = view.findViewById(R.id.tempBar5);
        tempBarList[5] = view.findViewById(R.id.tempBar6);
        tempBarList[6] = view.findViewById(R.id.tempBar7);
        tempBarList[7] = view.findViewById(R.id.tempBar8);
        tempBarList[8] = view.findViewById(R.id.tempBar9);
        tempBarList[9] = view.findViewById(R.id.tempBar10);
        tempBarList[10] = view.findViewById(R.id.tempBar11);
        tempBarList[11] = view.findViewById(R.id.tempBar12);

        voltTextList[0] = view.findViewById(R.id.voltage1);
        voltTextList[1] = view.findViewById(R.id.voltage2);
        voltTextList[2] = view.findViewById(R.id.voltage3);
        voltTextList[3] = view.findViewById(R.id.voltage4);
        voltTextList[4] = view.findViewById(R.id.voltage5);
        voltTextList[5] = view.findViewById(R.id.voltage6);
        voltTextList[6] = view.findViewById(R.id.voltage7);
        voltTextList[7] = view.findViewById(R.id.voltage8);
        voltTextList[8] = view.findViewById(R.id.voltage9);
        voltTextList[9] = view.findViewById(R.id.voltage10);
        voltTextList[10] = view.findViewById(R.id.voltage11);
        voltTextList[11] = view.findViewById(R.id.voltage12);

        voltBarList[0] = view.findViewById(R.id.voltBar1);
        voltBarList[1] = view.findViewById(R.id.voltBar2);
        voltBarList[2] = view.findViewById(R.id.voltBar3);
        voltBarList[3] = view.findViewById(R.id.voltBar4);
        voltBarList[4] = view.findViewById(R.id.voltBar5);
        voltBarList[5] = view.findViewById(R.id.voltBar6);
        voltBarList[6] = view.findViewById(R.id.voltBar7);
        voltBarList[7] = view.findViewById(R.id.voltBar8);
        voltBarList[8] = view.findViewById(R.id.voltBar9);
        voltBarList[9] = view.findViewById(R.id.voltBar10);
        voltBarList[10] = view.findViewById(R.id.voltBar11);
        voltBarList[11] = view.findViewById(R.id.voltBar12);



        errorTextBox = view.findViewById(R.id.errorTextBox);
        errorScrollView = view.findViewById(R.id.errorScrollView);

        // Add listener objects

        FrameLayout logoutFrameLayout = view.findViewById(R.id.logout);
        // Set an OnClickListener
        logoutFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment("S");
            }
        });

        // Find the FrameLayout by its ID
        FrameLayout switchFrameLayout = view.findViewById(R.id.switcher);

        // Set an OnClickListener
        switchFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment("D");
            }
        });

        // Find the FrameLayout by its ID
        FrameLayout settingsFrameLayout = view.findViewById(R.id.settings);

        // Set an OnClickListener
        settingsFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment("Set");
            }
        });

        for (int i=0; i < constraintLayouts.length; i++) {

            int finalI = i;
            constraintLayouts[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Ggf. args mit DetailedView.newInstance() bundlen, da spart man sich die Zeile hier. Denke das w#re best practice
                    DetailedViewState.getInstance().setState(finalI +1);
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, DetailedView.newInstance())
                            .addToBackStack(null)
                            .commit();

                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        connectedStatus = view.findViewById(R.id.connected_status);

        compactViewModel = new ViewModelProvider(this).get(CompactViewModel.class);
        for (int i = 0; i < buttons.length; i++) {
            animators[i] = createBlinkingAnimator(buttons[i]);
        }


        compactViewModel.getData().observe(getViewLifecycleOwner(), new Observer<CompactModel>() {
            @Override
            public void onChanged(CompactModel compactModel) {
                if(compactModel.isConnected())
                    connectedStatus.setImageResource(R.drawable.connected);
                else
                    connectedStatus.setImageResource(R.drawable.disconnected);
                for (int i = 0; i < segmentCount; i++) {
                    voltTextList[i].setText(compactModel.getVoltStr(i));
                    voltBarList[i].setLinePositions(compactModel.getVoltValue(i));

                    tempTextList[i].setText(compactModel.getTempStr(i));
                    tempBarList[i].setLinePositions(compactModel.getTempValue(i));
                }
                handleErrors(compactModel);
            }

        });
    }
    private void handleErrors(CompactModel compactModel) {
        errorTextBox.removeAllViews();
        //errorTextBox.setVisibility(View.GONE); // Initially hide the errorTextBox

        boolean anyError = Boolean.FALSE;
        for (int i = 0; i < constraintLayouts.length; i++) {

            if (compactModel.hasError(i)) {
                anyError = Boolean.TRUE;
                buttons[i].setBackgroundColor(Color.RED);
                startBlinkingAnimation(buttons[i], i);

                // dynamically creates textviews to display multiple error-messages

                // Create a new TextView for the error message
                TextView errorTextView = new TextView(getContext());
                errorTextView.setText(compactModel.getErrorText(i));
                errorTextView.setTextColor(Color.WHITE);
                errorTextView.setId(View.generateViewId());

                // Set constraints for TextView
                ConstraintLayout.LayoutParams textViewParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );

                // Set margins (8dp)
                int marginInPixels = (int) (8 * getResources().getDisplayMetrics().density);
                textViewParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);


                // If this is the first error message, constrain it to the top of the errorTextBox
                if (errorTextBox.getChildCount() == 0) {
                    textViewParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                } else {
                    // Otherwise, constrain it to the bottom of the last added TextView
                    textViewParams.topToBottom = errorTextBox.getChildAt(errorTextBox.getChildCount() - 1).getId();
//                    textViewParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                }

                // last message? bigger bottom margin! -- maybe fix this another way l8er
                if (i==constraintLayouts.length-1){
                    textViewParams.setMargins(marginInPixels, marginInPixels, marginInPixels, 3*marginInPixels);
                }

                errorTextView.setLayoutParams(textViewParams);

                // Add ConstraintLayout to errorTextBox
                errorTextBox.addView(errorTextView);
            } else {
                buttons[i].setBackgroundResource(R.drawable.segment_image);
                stopBlinkingAnimation(buttons[i], i);

            }
        }
        if(anyError) {errorScrollView.setVisibility(View.VISIBLE);} else {errorScrollView.setVisibility(View.GONE);}
    }

    private ObjectAnimator createBlinkingAnimator(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(BLINKING_RATE_MS); // duration of one tick/blink rate in ms
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        return animator;
    }

    private void startBlinkingAnimation(View view, int index) {
        ObjectAnimator animator = animators[index];
        if (animator != null && !animator.isRunning()) {
            animator.start();
        }
    }

    private void stopBlinkingAnimation(View view, int index) {
        ObjectAnimator animator = animators[index];
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

}

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

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.customElements.DetailedCellFragment;
import de.msbattery.batterymonitoringapp.model.DetailedModel;
import de.msbattery.batterymonitoringapp.model.DetailedViewModel;
import de.msbattery.batterymonitoringapp.graph.Graph;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailedView extends Fragment {

    private final int tableSize = 6;
    private TextView[] table;

    private TextView segmentNumber;
    private final int cellCount = 22;
    private DetailedCellFragment[] cellFragmentList;

    private Graph graph;

    private TextView error;

    private ImageView connectedStatus;


    private TextView icTempVal1;
    private TextView icTempVal2;

    public DetailedView() {
        // Required empty public constructor
    }

    // I know I know :(
    private void replaceFragment(String frag) {
        Fragment newFragment;
        switch (frag) {
            case "C":
                newFragment = CompactView.newInstance();
                break;
            case "Set":
                newFragment = SettingsFragment.newInstance(1, "D");
                break;
            default:
                newFragment = SimpleView.newInstance();
                break;
        }
        FragmentManager fragmentManager = getParentFragmentManager(); // or getChildFragmentManager() if nested
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, newFragment); // R.id.fragment_container is the ID of the container in the activity layout
        fragmentTransaction.commit();
    }

    public static DetailedView newInstance() {
        // INFO: Hier könnten wir ggf. Parameter an das Fragment übergeben, diese Methode ist nicht sinnlos
        // Siehe dazu bundle in SettingsFragment
        return new DetailedView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed_view, container, false);

        FrameLayout logoutFrameLayout = view.findViewById(R.id.logout);
        logoutFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment("S");
            }
        });

        FrameLayout switchFrameLayout = view.findViewById(R.id.switcher);
        switchFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment("C");
            }
        });

        FrameLayout settingsFrameLayout = view.findViewById(R.id.settings);
        settingsFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment("Set");
            }
        });

        ImageButton increaseSegmentButton = view.findViewById(R.id.increaseSegmentButton);
        increaseSegmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Here we could also prove if this move is legal and grey out the button if not
                int newState = DetailedViewState.getInstance().setState(DetailedViewState.getInstance().getState()+1);
                graph.reload(newState);
            }
        });

        ImageButton decreaseSegmentButton = view.findViewById(R.id.decreaseSegmentButton);
        decreaseSegmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Here we could also prove if this move is legal and grey out the button if not
                int newState = DetailedViewState.getInstance().setState(DetailedViewState.getInstance().getState()-1);
                graph.reload(newState);
            }
        });

        ConstraintLayout root = view.findViewById(R.id.detailed_container); // Make sure to assign an id to the inner LinearLayout

        // Start a single fragment transaction
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        int previousFragmentId = ConstraintLayout.LayoutParams.PARENT_ID;
        int half = cellCount/2;
        cellFragmentList = new DetailedCellFragment[cellCount];
        for (int i = 0; i < cellCount; i++) {
            String label = (i < half ? "A" + (i+1) : "B" + (i + 1 - half) );
            boolean withTemps = (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9 || i == 11 || i == 13 ||
                    i == 15 || i == 16 || i == 18 || i == 20);

            DetailedCellFragment fragment = DetailedCellFragment.newInstance(label,withTemps);
            cellFragmentList[i] = fragment;

            // Create a FrameLayout to host the fragment
            FrameLayout frameLayout = new FrameLayout(getContext());
            int frameLayoutId = View.generateViewId();
            frameLayout.setId(frameLayoutId);

            if (i % 2 == 0)
                frameLayout.setBackground(new ColorDrawable(getContext().getColor(R.color.darkmode_gray_light)));

            // Set FrameLayout layout parameters to match parent width
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            frameLayout.setLayoutParams(layoutParams);

            // Add the FrameLayout to the ConstraintLayout
            root.addView(frameLayout);

            // Add fragment to the FrameLayout
            fragmentTransaction.add(frameLayoutId, fragment);

            // Set constraints programmatically
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(root);

            if (previousFragmentId == ConstraintLayout.LayoutParams.PARENT_ID) {
                constraintSet.connect(frameLayoutId, ConstraintSet.TOP, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.TOP);
            } else {
                constraintSet.connect(frameLayoutId, ConstraintSet.TOP, previousFragmentId, ConstraintSet.BOTTOM, 0); // Set margin between fragments
            }

            constraintSet.connect(frameLayoutId, ConstraintSet.START, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(frameLayoutId, ConstraintSet.END, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.END);


            constraintSet.applyTo(root);

            previousFragmentId = frameLayoutId;
        }

        // Commit the transaction once after the loop
        fragmentTransaction.commit();

        //Add Graphs
        graph = new Graph(view, DetailedViewState.getInstance().getState());

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        DetailedViewModel detailedViewModel = new ViewModelProvider(this).get(DetailedViewModel.class);

        segmentNumber = view.findViewById(R.id.detailed_view_segment_number);

        table = new TextView[tableSize];
        table[0] = view.findViewById(R.id.detailed_min_voltage);
        table[1] = view.findViewById(R.id.detailed_med_voltage);
        table[2] = view.findViewById(R.id.detailed_max_voltage);

        table[3] = view.findViewById(R.id.detailed_min_temp);
        table[4] = view.findViewById(R.id.detailed_med_temp);
        table[5] = view.findViewById(R.id.detailed_max_temp);
        connectedStatus = view.findViewById(R.id.connected_status);

        icTempVal1 = view.findViewById(R.id.icTempVal);
        icTempVal2 = view.findViewById(R.id.icTempVal2);
        error = view.findViewById(R.id.error1);
        detailedViewModel.getData().observe(getViewLifecycleOwner(), new Observer<DetailedModel>() {
            @Override
            public void onChanged(DetailedModel detailedModel) {
                if(detailedModel.isConnected())
                    connectedStatus.setImageResource(R.drawable.connected);
                else
                    connectedStatus.setImageResource(R.drawable.disconnected);
                refreshData(detailedModel);
            }
        });
    }

    private void refreshData (DetailedModel detailedModel) {
        segmentNumber.setText(detailedModel.getSegment_number());
        for (int i = 0; i < cellCount; i++) {
            cellFragmentList[i].refreshData(detailedModel.getVoltageValue(i), detailedModel.getTempValue(i), detailedModel.getVoltageStr(i), detailedModel.getTempStr(i), detailedModel.getVoltStatus(i), detailedModel.getTempStatus(i));
        }
        for (int i= 0; i < tableSize; i++) {
            table[i].setText(detailedModel.getTable(i));
        }
        error.setText(detailedModel.getStatusStr());
        icTempVal1.setText(detailedModel.getIcTemp(0));
        icTempVal2.setText(detailedModel.getIcTemp(1));

    }

}
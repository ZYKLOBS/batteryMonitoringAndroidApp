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

package de.msbattery.batterymonitoringapp.customElements;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.structures.Status;

public class DetailedCellFragment extends Fragment {
    private static final String ARG_LABEL = "label";

    private TextView labelView;
    private TextView voltageValueView;
    private CustomStatusBarView voltageStatusView;

    private TextView tempValueView;
    private CustomStatusBarView tempStatusView;
    private static final String ARG_WITH_TEMP = "with temp?";

    private int warningColor = Color.RED;
    private final int normalColor = Color.WHITE;
    private int unplausibleColor = Color.YELLOW;
    public static DetailedCellFragment newInstance(String label, boolean with_Temp) {
        DetailedCellFragment fragment = new DetailedCellFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LABEL, label);
        args.putBoolean(ARG_WITH_TEMP, with_Temp);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detailed_view_cell_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        labelView = view.findViewById(R.id.label);
        voltageValueView = view.findViewById(R.id.voltageValue);
        voltageStatusView = view.findViewById(R.id.voltageStatus);
        tempValueView = view.findViewById(R.id.tempValue);
        tempStatusView = view.findViewById(R.id.tempStatus);
        if (getContext() != null)
            warningColor = ContextCompat.getColor(getContext(), R.color.voltageRed);

        if (getArguments() != null) {
            String label = getArguments().getString(ARG_LABEL);
            labelView.setText(label);

            if (!(getArguments().getBoolean(ARG_WITH_TEMP))) {
                tempStatusView.setVisibility(View.INVISIBLE);
                tempValueView.setVisibility(View.INVISIBLE);
            }
        }

    }

    public void refreshData(float voltage, float temperature, String voltageStr, String tempStr, Status voltStatus, Status tempStatus) {
        voltageStatusView.setLinePosition(voltage);
        voltageValueView.setText(voltageStr);

        int colorVolt = normalColor;
        if (voltStatus == Status.CRITICAL)
            colorVolt = warningColor;
        else if (voltStatus == Status.UNPLAUSIBLE)
            colorVolt = unplausibleColor;
        voltageValueView.setTextColor(colorVolt);

        int colorLabel = colorVolt;

        int colorTemp = normalColor;
        if (getArguments() != null && (getArguments().getBoolean(ARG_WITH_TEMP))) {
            tempStatusView.setLinePosition(temperature);
            tempValueView.setText(tempStr);

            if (tempStatus == Status.CRITICAL)
                colorTemp = warningColor;
            else if (tempStatus == Status.UNPLAUSIBLE)
                colorTemp = unplausibleColor;
            tempValueView.setTextColor(colorTemp);
        }

        if(colorLabel == normalColor) //if not warning color (=most critical color) already, then adapt
            colorLabel = colorTemp;

        //label should have "most critical color" (warningColor being more critical than unplausibleColor)
        labelView.setTextColor(colorLabel);

    }
}

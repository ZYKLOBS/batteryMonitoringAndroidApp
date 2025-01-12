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

package de.msbattery.batterymonitoringapp.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.Arrays;

import de.msbattery.batterymonitoringapp.parser.DataParser;
import de.msbattery.batterymonitoringapp.structures.Battery;
import de.msbattery.batterymonitoringapp.structures.Segment;
public class DatabaseViewModel extends AndroidViewModel{
    private final DataParser parser;
    private final int segmentCount = 12;
    private final MediatorLiveData<DatabaseModel> databaseModel = new MediatorLiveData<>();
    public DatabaseViewModel(@NonNull Application application) {
        super(application);
        parser = DataParser.getInstance();
        LiveData<Battery> batteryLiveData = parser.getBatteryLiveData();
        databaseModel.addSource(batteryLiveData, battery -> databaseModel.setValue(processData(battery)));
    }

    public LiveData<DatabaseModel> getData() {
        return databaseModel;
    }

    private DatabaseModel processData(Battery battery) {
        DatabaseModel model = new DatabaseModel();

        float[][] voltValueList = new float[segmentCount][];
        float[][] tempValueList = new float[segmentCount][];

        float[] voltMinList = new float[segmentCount];
        float[] voltMaxList = new float[segmentCount];
        float[] voltMeanList = new float[segmentCount];

        float[] tempMinList = new float[segmentCount];
        float[] tempMaxList = new float[segmentCount];
        float[] tempMeanList = new float[segmentCount];

        for (int i = 0; i < segmentCount; i++) {
            Segment segment = battery.getSegment(i);


            voltValueList[i] = mergeLists(segment.getCID(0).getVoltages(), segment.getCID(1).getVoltages());
            tempValueList[i] = mergeLists(segment.getCID(0).getTemperatures(), segment.getCID(1).getTemperatures());

            voltMinList[i] = Math.min(segment.getCID(0).getMinPlausibleVoltage(), segment.getCID(1).getMinPlausibleVoltage());
            voltMaxList[i] = Math.min(segment.getCID(0).getMaxPlausibleVoltage(), segment.getCID(1).getMaxPlausibleVoltage());
            voltMeanList[i] = getMean(segment.getCID(0).getPlausibleVoltages(), segment.getCID(1).getPlausibleVoltages());

            tempMinList[i] = Math.min(segment.getCID(0).getMinTempNoUnplausible(), segment.getCID(1).getMinTempNoUnplausible());
            tempMaxList[i] = Math.min(segment.getCID(0).getMaxTempNoUnplausible(), segment.getCID(1).getMaxTempNoUnplausible());
            tempMeanList[i] = getMean(segment.getCID(0).getPlausibleTemperatures(), segment.getCID(1).getPlausibleTemperatures());
        }

        model.setVoltValueList(voltValueList);
        model.setTempValueList(tempValueList);

        model.setVoltMinList(voltMinList);
        model.setVoltMaxList(voltMaxList);
        model.setVoltMeanList(voltMeanList);

        model.setTempMinList(tempMinList);
        model.setTempMaxList(tempMaxList);
        model.setTempMeanList(tempMeanList);

        return model;
    }

    private float[] mergeLists(float[] first, float[] second) {
        float[] both = Arrays.copyOf(first, first.length+second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    private float getMean(ArrayList<Float> v1, ArrayList<Float> v2) {
        float summedVolts = 0;

        for (float value: v1)
            summedVolts += value;

        for (float value: v2)
            summedVolts += value;

        return summedVolts/(v1.size() + v2.size());
    }
}

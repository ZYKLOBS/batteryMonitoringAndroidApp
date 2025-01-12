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

import de.msbattery.batterymonitoringapp.parser.DataParser;
import de.msbattery.batterymonitoringapp.structures.Battery;
import de.msbattery.batterymonitoringapp.structures.Segment;

/*Main purpose: parse the battery data into compactmodel structure and
* provide an observable of it for the view class*/
public class CompactViewModel extends AndroidViewModel {

    private final int segmentCount = 12;

    private final DataParser parser;
    private final MediatorLiveData<CompactModel> compactModelData = new MediatorLiveData<>();

    public CompactViewModel(@NonNull Application application) {
        super(application);
        parser = DataParser.getInstance();
        LiveData<Battery> batteryLiveData = parser.getBatteryLiveData();
        compactModelData.addSource(batteryLiveData, battery -> compactModelData.setValue(processData(battery)));
    }

    //parse raw battery data into compactmodel structure
    private CompactModel processData(Battery battery) {
        CompactModel model = new CompactModel();

        model.setConnected(battery.isConnected());

        float[][] voltValueList = new float[segmentCount][2]; //min, max for each segment
        String[] voltStrList = new String[segmentCount];
        float[][] tempValueList = new float[segmentCount][2]; //min, max for each segment
        String[] tempStrList = new String[segmentCount];

//        Status[] statusValueList = new Status[segmentCount];
//        String[] statusStrList = new String[segmentCount];

        Segment segment;
        for (int i = 0; i < segmentCount; i++) {
            segment = battery.getSegment(i);

            voltValueList[i] = new float[] { segment.getMinVoltage(), segment.getMaxVoltage() };
            tempValueList[i] = new float[] { segment.getMinTemp(), segment.getMaxTemp() };

            voltStrList[i] = Float.toString(segment.getMinVoltage()).substring(0,4) + "V - " + Float.toString(segment.getMaxVoltage()).substring(0,4) + "V";
            tempStrList[i] = Float.toString(segment.getMinTemp()).substring(0,4) + "°C - " + Float.toString(segment.getMaxTemp()).substring(0,4) + "°C";

//            statusValueList[i] = segment.getStatus();
//            statusStrList[i] = (segment.getStatus() != Status.GOOD) ? String.join("\n", segment.getStatusMessages()) : "";
        }

        model.setVoltValueList(voltValueList);
        model.setVoltStrList(voltStrList);

        model.setTempValueList(tempValueList);
        model.setTempStrList(tempStrList);

//        model.setStatusValueList(statusValueList);
//        model.setStatusStrList(statusStrList);

        model.setErrorList(battery.getErrorReport());
        return model;
    }


    public LiveData<CompactModel> getData() {
        return compactModelData;
    }
}

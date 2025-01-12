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

/*Main purpose: parse the battery data into simplemodel structure and
 * provide an observable of it for the view class*/
public class SimpleViewModel extends AndroidViewModel {
    private final DataParser parser;
    private final MediatorLiveData<SimpleModel> simpleModelData = new MediatorLiveData<>();

    public SimpleViewModel(@NonNull Application application) {
        super(application);
        parser = DataParser.getInstance();
        LiveData<Battery> batteryLiveData = parser.getBatteryLiveData();
        simpleModelData.addSource(batteryLiveData, battery -> simpleModelData.setValue(processData(battery)));
    }

    //parse raw battery data into simplemodel structure
    private SimpleModel processData(Battery battery) {
        SimpleModel model = new SimpleModel();
        model.setConnected(battery.isConnected());
        model.setStatus(battery.getOverallStatus());
        model.setErrorMessages(battery.getErrorReportsAsString());

        return model;
    }

    public LiveData<SimpleModel> getData() {
        return simpleModelData;
    }

}

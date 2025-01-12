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
import de.msbattery.batterymonitoringapp.structures.CID;
import de.msbattery.batterymonitoringapp.structures.ErrorMessage;
import de.msbattery.batterymonitoringapp.structures.Segment;
import de.msbattery.batterymonitoringapp.structures.Status;
import de.msbattery.batterymonitoringapp.views.DetailedViewState;

/*Main purpose: parse the battery data into detailedmodel structure and
 * provide an observable of it for the view class*/
public class DetailedViewModel extends AndroidViewModel {
    private final DataParser parser;

    private final DetailedViewState detailedViewState;
    private final MediatorLiveData<DetailedModel> detailedModelData = new MediatorLiveData<>();

    private DetailedModel model;

    public DetailedViewModel(@NonNull Application application) {
        super(application);
        parser = DataParser.getInstance();
        detailedViewState = DetailedViewState.getInstance();

        LiveData<Battery> batteryLiveData = parser.getBatteryLiveData();
        detailedModelData.addSource(batteryLiveData, battery -> detailedModelData.setValue(processData(battery)));

        LiveData<Integer> segmentStateLiveData = detailedViewState.getStateLiveData();
        detailedModelData.addSource(segmentStateLiveData, segmentNumber -> detailedModelData.setValue(processState(segmentNumber)));
    }

    //parse raw battery data into detailedmodel structure
    private DetailedModel processData(Battery battery) {
        DetailedModel model = new DetailedModel();

        model.setConnected(battery.isConnected());
        int segment_number = DetailedViewState.getInstance().getState();

        String segment_number_string = "Segment ".concat(Integer.toString(segment_number));
        model.setSegment_number(segment_number_string);

        Segment seg = battery.getSegment(segment_number-1); //Segment 1 hat Index 0
        CID cid0 = seg.getCID(0);
        CID cid1 = seg.getCID(1);

        String[] voltageStrings = createVoltageStrings(cid0,cid1);
        model.setVoltageStrList(voltageStrings);

        String[] temperatureStrings = createTemperatureStrings(cid0,cid1);
        model.setTempStrList(temperatureStrings);

        String[] tableStrings = createTableString(seg);
        model.setTable(tableStrings);

        model.setVoltageValueList(getVoltageValues(cid0.getVoltages() ,cid1.getVoltages()));
        model.setVoltStatusList(getVoltStatusList(cid0.getVoltStatus(), cid1.getVoltStatus()));

        model.setTempValueList(getTempValueList(cid0.getTemperatures(), cid1.getTemperatures(), cid0.getVoltages().length, cid1.getVoltages().length));
        model.setTempStatusList(getTempStatusList(cid0.getTempStatus(), cid1.getTempStatus(), cid0.getVoltages().length, cid1.getVoltages().length));

        model.setStatus(seg.getStatus());

        model.setStatusMessages(String.join("\n", seg.getStatusMessages()));
        ArrayList<String> statArr = new ArrayList<>(seg.getStatusMessages());
        statArr.addAll(seg.getStatusMessagesDetailed());
        model.setStatusMessages(String.join("\n", statArr));

        model.setIcTemp(new String[] {cid0.getIcTemperature() + " °C", cid1.getIcTemperature() + " °C" });

        this.model = model;

        return model;
    }

    private Status[] getVoltStatusList(Status[] cid0Status, Status[] cid1Status) {
        Status[] both = Arrays.copyOf(cid0Status, cid0Status.length+cid1Status.length);
        System.arraycopy(cid1Status, 0, both, cid0Status.length, cid1Status.length);
        return both;
    }

    private Status[] getTempStatusList(Status[] cid0Status, Status[] cid1Status, int len0, int len1) {
        Status[] tempStatus = new Status[len0 + len1];

        int indexCounter = 0;
        int i = 0;
        while (i < len0) {
            if (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9) {
                tempStatus[i] = cid0Status[indexCounter];
                indexCounter++;
            }
            else
                tempStatus[i] = Status.GOOD;
            i++;
        }

        indexCounter = 0;
        i = 0;
        while (i < len1) {
            if (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9) {
                tempStatus[i + len0] = cid1Status[indexCounter];
                indexCounter++;
            }
            else
                tempStatus[i + len0] = Status.GOOD;
            i++;
        }
        return tempStatus;
    }

    /*Cells 1, 3, 5, 6, 8, 10 have a temperature sensor. Set values for other cells to Float.MIN to make processing easier*/
    private float[] getTempValueList(float[] temps0, float[] temps1, int len0, int len1) {
        float[] temperatureValues = new float[len0 + len1];

        int indexCounter = 0;
        int i = 0;
        while (i < len0) {
            if (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9) {
                temperatureValues[i] = temps0[indexCounter];
                indexCounter++;
            }
            else
                temperatureValues[i] = Float.MIN_VALUE;
            i++;
        }

        indexCounter = 0;
        i = 0;
        while (i < len1) {
            if (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9) {
                temperatureValues[i + len0] = temps1[indexCounter];
                indexCounter++;
            }
            else
                temperatureValues[i + len0] = Float.MIN_VALUE;
            i++;
        }
        return temperatureValues;
    }

    private float[] getVoltageValues(float[] voltages0, float[] voltages1) {
        float[] concated = Arrays.copyOf(voltages0, voltages0.length + voltages1.length);
        System.arraycopy(voltages1, 0, concated, voltages0.length, voltages1.length);
        return  concated;
    }


    private String[] createVoltageStrings(CID cid0, CID cid1){
        float[] voltages0 = cid0.getVoltages();
        float[] voltages1 = cid1.getVoltages();
        String[] voltageString = new String[voltages0.length+voltages1.length];
        for (int i = 0; i < voltages0.length; i++) {
            voltageString[i] = Float.toString(voltages0[i]).substring(0,5).concat(" V");
        }
        for (int i = 0; i < voltages1.length; i++) {
            voltageString[i+voltages0.length] = Float.toString(voltages1[i]).substring(0,5).concat(" V");
        }
        return voltageString;
    }

    /*Cells 1, 3, 5, 6, 8, 10 have a temperature sensor. Set values for other cells to  "" to make processing easier*/
    private String[] createTemperatureStrings(CID cid0, CID cid1) {
        float[] temps0 = cid0.getTemperatures();
        float[] temps1 = cid1.getTemperatures();
        int len0 = cid0.getVoltages().length;
        int len1 = cid1.getVoltages().length;
        String[] temperatureString = new String[len0 + len1];

        int indexCounter = 0;
        int i = 0;
        while (i < len0) {
            if (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9) {
                temperatureString[i] = Float.toString(temps0[indexCounter]).substring(0, 4).concat(" °C");
                indexCounter++;
            }
            else
                temperatureString[i] = "";
            i++;
        }

        indexCounter = 0;
        i = 0;
        while (i <len1) {
            if (i == 0 || i == 2 || i == 4 || i == 5 || i == 7 || i == 9) {
                temperatureString[i + len0] = Float.toString(temps1[indexCounter]).substring(0, 4).concat(" °C");
                indexCounter++;
            }
            else
                temperatureString[i + len0] = "";
            i++;
        }
//        for (int i = 0; i < temps1.length; i++) {
//            temperatureString[i+temps0.length] = Float.toString(temps1[i]).substring(0,4).concat(" °C");
//        }
        return temperatureString;
    }

    private String[] createTableString(Segment segment){
        String[] tableStrings = new String[6];
        tableStrings[0] = Float.toString(segment.getMinVoltage()).substring(0,5).concat(" V");
        tableStrings[1] = Float.toString(segment.getMedVoltage()).substring(0,5).concat(" V");
        tableStrings[2] = Float.toString(segment.getMaxVoltage()).substring(0,5).concat(" V");
        tableStrings[3] = Float.toString(segment.getMinTemp()).substring(0,4).concat(" °C");
        tableStrings[4] = Float.toString(segment.getMedTemp()).substring(0,4).concat(" °C");
        tableStrings[5] = Float.toString(segment.getMaxTemp()).substring(0,4).concat(" °C");
        return tableStrings;
    }

    public LiveData<DetailedModel> getData() {
        return detailedModelData;
    }

    private DetailedModel processState(Integer segmentNumber) {
        if (model != null) {
            // this was the first thing that crashed when the websocket connection fails
            this.model.setSegment_number("Segment ".concat(Integer.toString(segmentNumber)));
        }
        else {
            model = createErrorModel(segmentNumber);
        }
        return model;
    }

    private DetailedModel createErrorModel(int segmentNumber){
        DetailedModel errorModel = new DetailedModel();
        errorModel.setSegment_number("Segment ".concat(Integer.toString(segmentNumber)));
        String[] voltageErrorStrings = new String[22];
        String[] tempErrorStrings = new String[22];
        String[] tableErrorStrings = new String[22];
        for (int i = 0; i<22; i++){
            voltageErrorStrings[i] = ErrorMessage.VOLTAGE_ERROR.getMessage();
            tempErrorStrings[i] = ErrorMessage.TEMPERATURE_ERROR.getMessage();
            tableErrorStrings[i] = ErrorMessage.TABLE_ERROR.getMessage();
        }
        errorModel.setVoltageStrList(voltageErrorStrings);
        errorModel.setTempStrList(tempErrorStrings);
        errorModel.setTable(tableErrorStrings);
        return errorModel;
    }
}

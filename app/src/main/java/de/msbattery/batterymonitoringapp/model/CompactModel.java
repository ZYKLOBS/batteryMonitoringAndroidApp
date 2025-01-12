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

import de.msbattery.batterymonitoringapp.structures.ErrorMessage;

import java.util.ArrayList;


/*This is the class that holds all Data that is to be displayed in the CompactView.
 * Processing inside the CompactView class should be contained to an absolute minimum,
 * therefore please hold the data in the right format here already.*/
public class CompactModel {
    private float[][] tempValueList;
    private float[][] voltValueList;
    private String[] tempStrList;
    private String[] voltStrList;
//    private Status[] statusValueList;
//    private String[] statusStrList;
    private ArrayList<String> errorList;

    private boolean connected;

    public float[] getTempValue(int segmentIndex) {
        if (this.tempValueList != null)
            return this.tempValueList[segmentIndex];
        return new float[]{Float.MIN_VALUE, Float.MIN_VALUE};
    }

    public float[] getVoltValue(int segmentIndex) {
        if (this.tempValueList != null)
            return this.voltValueList[segmentIndex];
        return new float[]{Float.MIN_VALUE, Float.MIN_VALUE};
    }

    public String getTempStr(int segmentIndex) {
        if (this.tempStrList != null)
            return this.tempStrList[segmentIndex];
        return ErrorMessage.TEMPERATURE_ERROR.getMessage();
    }

    public String getVoltStr(int segmentIndex) {
        if (this.voltStrList != null)
            return this.voltStrList[segmentIndex];
        return ErrorMessage.VOLTAGE_ERROR.getMessage();
    }

    public boolean isConnected() {
        return connected;
    }


    public String getErrorText(int i) {
        if (this.errorList == null)
            return ErrorMessage.STATUS_ERROR.getMessage();
        return "Segment " + (i + 1) + ": " + this.errorList.get(i);
    }

    public void setErrorList(ArrayList<String> eList) {
        this.errorList = eList;
    }

//    public Status getStatus(int segmentIndex) {
//        if (this.statusValueList != null)
//            return this.statusValueList[segmentIndex];
//        return Status.FAILURE;
//    }
//
//    public String getStatusStr(int segmentIndex) {
//        if (this.statusStrList != null)
//            return this.statusStrList[segmentIndex];
//        return "";
//    }

    public void setTempValueList(float[][] tempValueList) {
        this.tempValueList = tempValueList;
    }

    public void setVoltValueList(float[][] voltValueList) {
        this.voltValueList = voltValueList;
    }

    public void setTempStrList(String[] tempStrList) {
        this.tempStrList = tempStrList;
    }

    public void setVoltStrList(String[] voltStrList) {
        this.voltStrList = voltStrList;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Boolean hasError(int SIndex) {
        return !errorList.get(SIndex).equals("[GOOD]");
    }
}
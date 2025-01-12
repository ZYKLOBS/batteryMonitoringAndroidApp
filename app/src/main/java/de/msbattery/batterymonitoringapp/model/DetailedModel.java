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
import de.msbattery.batterymonitoringapp.structures.Status;

/*This is the class that holds all Data that is to be displayed in the DetailedView.
 * Processing inside the DetailedView class should be contained to an absolute minimum,
 * therefore please hold the data in the right format here already.*/
public class DetailedModel {
    private String[] voltageStrList;
    private String[] tempStrList;
    private String[] table;

    private boolean connected;

    private float[] voltageValueList;
    private float[] tempValueList;
    private String[] icTemp;

    private String segment_number;

    private String statusMessages;
    private Status status;

    private Status[] tempStatusList;
    private Status[] voltStatusList;

    public String getVoltageStr(int index){
        if (voltageStrList != null)
            return voltageStrList[index];
        return ErrorMessage.VOLTAGE_ERROR.getMessage();
    }
    public String getTempStr(int index) {
        if (tempStrList != null)
            return tempStrList[index];
        return ErrorMessage.TEMPERATURE_ERROR.getMessage();
    }

    public void setSegment_number(String segment_number) {
        this.segment_number = segment_number;
    }

    public String getSegment_number() {
        return segment_number;
    }

    public void setTable(String[] table) {
        this.table = table;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getTable(int index) {
        return table[index];
    }

    public String getRandomText() {
        return Double.toString(Math.random());
    }

    public boolean isConnected() {
        return connected;
    }

    public float getVoltageValue(int index) {
        if (voltageValueList != null)
            return this.voltageValueList[index];
        return Float.MIN_VALUE;
    }

    public float getTempValue(int index) {
        if (this.tempValueList != null)
            return this.tempValueList[index];
        return Float.MIN_VALUE;
    }

    public String getIcTemp(int cidIndex) {
        if (this.icTemp != null)
            return this.icTemp[cidIndex];
        return ErrorMessage.TEMPERATURE_ERROR.getMessage();
    }

    public void setIcTemp(String[] temp) {
        this.icTemp = temp;
    }

    public Status getStatus() {
        if (this.status != null)
            return this.status;
        return Status.FAILURE;
    }

    public String getStatusStr() {
        if (this.getStatus() == Status.FAILURE || this.statusMessages == null)
            return ErrorMessage.STATUS_ERROR.getMessage();
        return this.statusMessages;
    }

    public void setTempValueList(float[] tempValueList) {
        this.tempValueList = tempValueList;
    }

    public void setVoltageStrList(String[] voltages) {
        this.voltageStrList = voltages;
    }

    public void setTempStrList(String[] temps) {
        this.tempStrList = temps;
    }

    public void setVoltageValueList(float[] voltageValueList) {
        this.voltageValueList = voltageValueList;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStatusMessages(String statusMessages) {
        this.statusMessages = statusMessages;
    }

    public Status getTempStatus(int index) {
        if (tempStatusList != null)
            return tempStatusList[index];
        return Status.FAILURE;
    }

    public void setTempStatusList(Status[] tempStatus) {
        this.tempStatusList = tempStatus;
    }

    public Status getVoltStatus(int index) {
        if(voltStatusList != null)
            return voltStatusList[index];
        return Status.FAILURE;
    }

    public void setVoltStatusList(Status[] voltStatus) { this.voltStatusList = voltStatus; }

}

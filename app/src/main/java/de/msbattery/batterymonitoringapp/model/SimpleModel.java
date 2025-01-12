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

import de.msbattery.batterymonitoringapp.structures.Status;


/*This is the class that holds all Data that is to be displayed in the SimpleView.
* Processing inside the SimpleView class should be contained to an absolute minimum,
* therefore please hold the data in the right format here already.*/
public class SimpleModel {

    //TODO: should be able to get emergency contact data too

    private boolean connected;
    private int chargingStatus;
    private Status status;
    private String errorMessages;

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getStatusString() {
        switch(status) {
            case GOOD:
                return "OKAY";
            case CRITICAL:
                return "CRITICAL";
            default:
                return "NOT SET";
        }
    }

    public Status getStatus() {
        return this.status;
    }

    public int getChargingStatus() {
        return chargingStatus;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setChargingStatus(int chargingStatus) {
        this.chargingStatus = chargingStatus;
    }

    public void setErrorMessages(String errorMessages) {
        this.errorMessages = errorMessages;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getErrorMessages() {
        return errorMessages;
    }

    public String getInfoField() {
        switch (status) {
            case GOOD:
                return "";

            case CRITICAL:
                return "DANGER!\nCONTACT EMERGENCY CONTACTS!";

            default:
                return "Unknown Error";
        }
    }
}

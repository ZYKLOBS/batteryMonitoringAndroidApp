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

package de.msbattery.batterymonitoringapp.structures;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class CID {

    private float[] voltages;
    private float[] temperatures;
    private Status[] tempStatus = new Status[]{Status.GOOD, Status.GOOD, Status.GOOD, Status.GOOD, Status.GOOD, Status.GOOD};
    private Status[] voltStatus;
    private float stackVoltage;
    private float icTemperature;
    private short[] masterFaultReg;
    private short[] secFaultReg;
    private float[] oldVoltages;
    private float[] oldTemperatures;


    private Status status;
    private ArrayList<String> statusMessages = new ArrayList<>(); //statusMessages für die gesamte CID
    private ArrayList<String> statusMessagesDetailed = new ArrayList<>(); //statusMessages in den einzelnen zellen

    private boolean cellOTFault = false;
    private boolean cellUTFault = false;
    private boolean cellOVFault = false;
    private boolean cellUVFault = false;

    private static final float minPlausibleTemp = -20f;
    private static final float maxPlausibleTemp = 80f;
    private static final float minPlausibleVolt = 2f;
    private static final float maxPlausibleVolt = 5f;


    /*map index in temperature List to the Cell No
    * in this case, cell No starts counting at 1 (so usually cell 1 to 11)*/
    Map<Integer, Integer> indexToCellNo = Map.of(0,1,
            1,3,
            2,5,
            3,6,
            4,8,
            5,10);

    public CID(float[] voltages, float[] stackTemps, float stackVoltage, float icTemperature, short[] masterFaultReg, short[] secFaultReg, float[] oldVoltages, float[] oldTemperatures) {
        this.voltages = voltages;
        this.voltStatus = new Status[voltages.length];
        this.temperatures = stackTemps;
        this.stackVoltage = stackVoltage;
        this.icTemperature = icTemperature;
        this.masterFaultReg = masterFaultReg;
        this.secFaultReg = secFaultReg;
        this.oldVoltages = oldVoltages;
        this.oldTemperatures = oldTemperatures;
        this.determineStatus();
    }

    public float[] getVoltages(){return this.voltages;}

    public ArrayList<Float> getPlausibleVoltages() {
        ArrayList<Float> plausibleVolts = new ArrayList<>();
        for (float volt: this.voltages) {
            if ((volt > minPlausibleVolt) && (volt < maxPlausibleVolt))
                plausibleVolts.add(volt);
        }
        return plausibleVolts;
    }

    public ArrayList<Float> getPlausibleTemperatures() {
        ArrayList<Float> plausibleTemps = new ArrayList<>();
        for (float temps: this.temperatures) {
            if ((temps > minPlausibleTemp) && (temps < maxPlausibleTemp))
                plausibleTemps.add(temps);
        }
        return plausibleTemps;
    }
    public float getMinPlausibleVoltage(){
        float[] copy = Arrays.copyOf(voltages, voltages.length);
        for (int i = 0; i < copy.length; i++){
            if(copy[i] < minPlausibleVolt)
                copy[i] = getMax(copy);
        }
        return getMin(copy);
    }

    public float getMedVoltage(){return getMed(voltages);}
    public float getMaxPlausibleVoltage(){
        float[] copy = Arrays.copyOf(voltages, voltages.length);
        for (int i = 0; i < copy.length; i++){
            if(copy[i] > maxPlausibleVolt)
                copy[i] = getMax(copy);
        }
        return getMax(copy);
    }
    public float getMinTempNoUnplausible(){
        float[] tempCopy = Arrays.copyOf(temperatures, temperatures.length);
        for (int i = 0; i < tempCopy.length; i++) {
            if(tempCopy[i] < minPlausibleTemp)
                tempCopy[i] = getMax(tempCopy);
        }
        return getMin(tempCopy);
    }

    public float getMedTemp(){return getMed(temperatures);}
    public float getMaxTempNoUnplausible(){
        float[] tempCopy = Arrays.copyOf(temperatures, temperatures.length);
        for (int i = 0; i < tempCopy.length; i++) {
            if(tempCopy[i] > maxPlausibleTemp)
                tempCopy[i] = getMin(tempCopy);
        }
        return getMax(tempCopy);}

    public float getMin(float[] array){
        float min = array[0];
        for (int i = 1; i < array.length;  i++){
            min = Math.min(min, array[i]);
        }
        return min;
    }
    private float getMed(float[] array){
        float[] aCopy = array.clone();
        Arrays.sort(aCopy);
        if (aCopy.length%2==1){
            return aCopy[(int)(aCopy.length/2)];
        }
        return (aCopy[aCopy.length/2 -1] + aCopy[(int)(aCopy.length/2)]) /2;
    }
    private float getMax(float[] array){
        float max = array[0];
        for (int i = 1; i < array.length;  i++){
            max = Math.max(max, array[i]);
        }
        return max;
    }

    public float[] getTemperatures() {
        return temperatures;
    }

    public float getStackVoltage() {
        return stackVoltage;
    }

    public float getIcTemperature() {
        return icTemperature;
    }

    public short[] getMasterFaultReg() {
        return masterFaultReg;
    }

    public short[] getSecFaultReg() {
        return secFaultReg;
    }

    public Status getStatus() {
        if(status == null)
            determineStatus();
        return this.status;
    }

    public ArrayList<String> getStatusMessages() {
        if(status == null)
            determineStatus();
        return statusMessages;
    }

    public ArrayList<String> getStatusMessagesDetailed() {
        if (status == null)
            determineStatus();
        return statusMessagesDetailed;
    }

    private void determineStatus() {
        this.statusMessages = new ArrayList<>();
        this.status = Status.GOOD;
        this.cellOTFault = false;
        this.cellUTFault = false;
        this.cellOVFault = false;
        this.cellUVFault = false;

        checkVoltageChange();
        checkTempChange();

        //assumption: secondary fault registers are ordered as shown on slide 16
        //since we converted to String, bit 15 is at register.chatAt(0)

        // FAULT2
        String FAULT2 = shortToString(masterFaultReg[1]);
        checkFault2Register(FAULT2);
        //Log.d("FAULT2", FAULT2);

        // FAULT3
        String FAULT3 = shortToString(masterFaultReg[2]);
        //no method for checking bc only relevant for cell balancing time (no critical errors)
        //Log.d("FAULT3", FAULT3);

        // CELL_OV_FLT
        String CELL_OV_FLT = shortToString(secFaultReg[0]);
        this.cellOVFault = checkRegForVoltFault(CELL_OV_FLT, ErrorMessage.OVERVOLTAGE);

        // CELL_UV_FLT
        String CELL_UV_FLT = shortToString(secFaultReg[1]);
        this.cellUVFault =  checkRegForVoltFault(CELL_UV_FLT, ErrorMessage.UNDERVOLTAGE);

        // AN_OT_UT_FLT
        String AN_OT_UT_FLT = shortToString(secFaultReg[2]);
        checkN_OT_UT_FLTRegister(AN_OT_UT_FLT);

        // COM_STATUS - no fatal errors here
        String COM_STATUS = shortToString(secFaultReg[3]);

        // CB_OPEN_FLT
        String CB_OPEN_FLT = shortToString(secFaultReg[4]);
        if(checkRegForFault(CB_OPEN_FLT, ErrorMessage.OPENLOAD))
            setCritialStatus(ErrorMessage.OPENLOAD);

        // CB_SHORT_FLT
        String CB_SHORT_FLT = shortToString(secFaultReg[5]);
        if(checkRegForFault(CB_SHORT_FLT, ErrorMessage.SHORTCIRCUIT))
            setCritialStatus(ErrorMessage.SHORTCIRCUIT);

        checkMeteredValues();
        // FAULT1
        String FAULT1 = shortToString(masterFaultReg[0]);
        checkFault1Register(FAULT1); //check last so data from secondary registers can be accessed during checking
        //Log.d("FAULT1", FAULT1);

    }


    private void checkMeteredValues() {
        for (int i = 0; i < temperatures.length; i++) {
            if (temperatures[i] < minPlausibleTemp || temperatures[i] > maxPlausibleTemp)
                this.tempStatus[i] = Status.UNPLAUSIBLE;
        }

        for (int i = 0; i < voltages.length; i++) {
            if(voltages[i] < minPlausibleVolt || voltages[i] > maxPlausibleVolt)
                this.voltStatus[i] = Status.UNPLAUSIBLE;
        }
    }

    private String shortToString(short value) {
        String binaryString = Integer.toBinaryString(value & 0xFFFF);

        // Padding
        while (binaryString.length() < 16) {
            binaryString = "0" + binaryString;
        }

        return binaryString;
    }

    private void checkFault1Register(String fault1) {
        /*relevant bits : 12 (IC supply voltage too high), 11 (IC supp.y voltage is low),
        * 3 (overtemperature), 2 (undertemperature), 1(overvoltage), 0 (undervoltage)
        * laut nicolas sollen under-/over temperature und fault nur dann gesetzt werden, wenn das in den detaillierten Fehler-
        * registern auch erkannt wurde
        * Um alles konsistent zu halten, also einfach hinzufügen, sobald ein Fehler in den secondary registern erkannt wurde*/

//        if(fault1.charAt(15 - 0) == '1' && this.cellUVFault) setCritialStatus(ErrorMessage.UNDERVOLTAGE);
//        if(fault1.charAt(15 - 1) == '1' && this.cellOVFault) setCritialStatus(ErrorMessage.OVERVOLTAGE);
//        if(fault1.charAt(15 - 2) == '1' && this.cellUTFault) setCritialStatus(ErrorMessage.UNDERTEMPERATURE);
//        if(fault1.charAt(15 - 3) == '1' && this.cellOTFault) setCritialStatus(ErrorMessage.OVERTEMPERATURE);
        if(this.cellUVFault) setCritialStatus(ErrorMessage.UNDERVOLTAGE);
        if(this.cellOVFault) setCritialStatus(ErrorMessage.OVERVOLTAGE);
        if(this.cellUTFault) setCritialStatus(ErrorMessage.UNDERTEMPERATURE);
        if(this.cellOTFault) setCritialStatus(ErrorMessage.OVERTEMPERATURE);
        if(fault1.charAt(15 - 11) == '1') setCritialStatus(ErrorMessage.IC_SUPPLY_LOW);
        if(fault1.charAt(15 - 12) == '1') setCritialStatus(ErrorMessage.IC_SUPPLY_HIGH);
    }

    private void checkFault2Register(String fault2) {
        /*relevant bits: 13, 12, 11,10,9,2,1,0 (IC malfunction (Analoy supply is part of IC too)),
        8 (IC overheating),
        * 3 (Open load on cell balancer), 4(short circuit on cell balancer) */

        if(fault2.charAt(15 - 0) == '1' || fault2.charAt(15 - 1) == '1' || fault2.charAt(15 - 2) == '1' ||
                fault2.charAt(15 - 9) == '1' || fault2.charAt(15 - 10) == '1' || fault2.charAt(15 - 11) == '1' ||
                fault2.charAt(15 - 12) == '1' || fault2.charAt(15 - 13) == '1')
            setCritialStatus(ErrorMessage.IC_MALFUNCTION);

        if(fault2.charAt(15 - 8) == '1') setCritialStatus(ErrorMessage.IC_OVERHEATING);

        if(fault2.charAt(15 - 3) == '1') setCritialStatus(ErrorMessage.OPENLOAD_BALANCER);
        if(fault2.charAt(15 - 4) == '1') setCritialStatus(ErrorMessage.SHORTCIRCUIT_BALANCER);
    }

    private void checkN_OT_UT_FLTRegister(String register) {
        for (int i = 1; i <=6; i++) {
            if(register.charAt(15 - i) == '1') {

                if(this.temperatures[i - 1] >= minPlausibleTemp) {
                    //temperature is plausible bc undertemp is not too low
                    setCriticalStatusInCell(ErrorMessage.UNDERTEMPERATURE, indexToCellNo.get(i - 1) - 1);
                    this.tempStatus[i - 1] = Status.CRITICAL;
                    this.cellUTFault = true;
                } else {
                    this.tempStatus[i - 1] = Status.UNPLAUSIBLE;
                    //do not change status of cell but add unplausible as info
                    this.statusMessagesDetailed.add((indexToCellNo.get(i - 1)) + ": " + ErrorMessage.TEMP_UNPLAUSIBLE.getMessage());
                }
            }

        }

        for(int i = 9; i<= 14; i++) {
            if(register.charAt(15 - i) == '1') {
                //check whether temp value is plausible
                if (this.temperatures[i - 9] <= maxPlausibleTemp) {
                    setCriticalStatusInCell(ErrorMessage.OVERTEMPERATURE, indexToCellNo.get(i - 9) - 1);
                    this.tempStatus[i - 9] = Status.CRITICAL;
                    this.cellOTFault = true;
                } else {
                    this.tempStatus[i - 9] = Status.UNPLAUSIBLE;
                    //do not change status of cell but add unplausible as info
                    this.statusMessagesDetailed.add((indexToCellNo.get(i - 1)) + ": " + ErrorMessage.TEMP_UNPLAUSIBLE.getMessage());
                }
            }

        }
    }

    private boolean checkRegForFault(String register, ErrorMessage msg) {
        boolean foundFault = false;
        int cellI = 10; //Zellindex passend zur Laufvariablen i
        for (int i = 2; i < register.length(); i++) { //the two most significant bits do not contain information -> start behind them
            if (i == (15 - 6) || i == (15 - 5) || i == (15 - 4)) continue;  //die zellterminals 7,6,5 (also bits 6,5,4) müssen immer ignoriert werden
            if(register.charAt(i) == '1') {
                setCriticalStatusInCell(msg, cellI);
                foundFault = true;
            }

            cellI--;
        }
        return foundFault;
    }

    private boolean checkRegForVoltFault(String register, ErrorMessage msg) {
        boolean foundFault = false;
        Status currStatus;
        int cellI = 10; //Zellindex passend zur Laufvariablen i

        for (int i = 2; i < register.length(); i++) { //the two most significant bits do not contain information -> start behind them

            currStatus = Status.GOOD;
            if (i == (15 - 6) || i == (15 - 5) || i == (15 - 4)) continue;  //die zellterminals 7,6,5 (also bits 6,5,4) müssen immer ignoriert werden

            if(register.charAt(i) == '1') { //error detected

                //check if metered value is plausible which means detected error is plausible
                if ((msg == ErrorMessage.OVERVOLTAGE && this.voltages[cellI] > maxPlausibleVolt) ||
                        (msg == ErrorMessage.UNDERVOLTAGE && this.voltages[cellI] < minPlausibleVolt)) {
                    //not plausible
                    currStatus = Status.UNPLAUSIBLE;
                    this.statusMessagesDetailed.add(cellI + ": " + ErrorMessage.VOLT_UNPLAUSIBLE);
                } else {
                    //plausibly critical
                    setCriticalStatusInCell(msg, cellI);
                    currStatus = Status.CRITICAL;
                    foundFault = true;
                }

            }

            if(this.voltStatus[cellI] == null)
                this.voltStatus[cellI] = currStatus;
            else if (currStatus.v() > this.voltStatus[cellI].v())
                this.voltStatus[cellI] = currStatus;

            cellI--;
        }
        return foundFault;
    }

    private void setCritialStatus(ErrorMessage error) {
        this.statusMessages.add(0," " + error.getMessage()); //put more generic infos at front of error list
        this.status = Status.CRITICAL;
    }

    private void setCriticalStatusInCell(ErrorMessage error, int cellIndex) {
        this.statusMessagesDetailed.add(0, (cellIndex + 1) + ": " + error.getMessage());
        this.status = Status.CRITICAL;
    }

    public Status[] getTempStatus() {
        return this.tempStatus;
    }

    public Status[] getVoltStatus() {
        return voltStatus;
    }
    private void checkVoltageChange(){
        if (oldVoltages == null){return;}
        boolean foundDischarge = false;
        boolean foundCharge = false;
        float timeDifference = (float)OldBattery.getInstance().getTimeDifference().getNano()/1000000000L;
        timeDifference += (float)OldBattery.getInstance().getTimeDifference().getSeconds();
        float maxChange = OldBattery.getInstance().getMaxVoltageChange();

        for (int i = 0; i < voltages.length; i++){
            if (voltages[i] < oldVoltages[i] - maxChange*timeDifference){
                setCriticalStatusInCell(ErrorMessage.DISCHARGING_TOO_FAST,i);
                foundDischarge = true;
            }
            if (voltages[i] > oldVoltages[i] + maxChange*timeDifference) {
                setCriticalStatusInCell(ErrorMessage.CHARGING_TOO_FAST,i);
                foundCharge = true;
            }
        }

        //add to general error list once
        if (foundDischarge)
            setCritialStatus(ErrorMessage.DISCHARGING_TOO_FAST);
        if(foundCharge)
            setCritialStatus(ErrorMessage.CHARGING_TOO_FAST);
    }

    private void checkTempChange(){
        if (oldTemperatures == null){return;}
        boolean foundHeating = false;
        boolean foundCooling = false;
        float timeDifference = (float)OldBattery.getInstance().getTimeDifference().getNano()/1000000000L;
        timeDifference += OldBattery.getInstance().getTimeDifference().getSeconds();
        float maxChange = OldBattery.getInstance().getMaxTempChange();

        for (int i = 0; i < temperatures.length; i++){
            if (temperatures[i] < oldTemperatures[i] - maxChange*timeDifference) {
                setCriticalStatusInCell(ErrorMessage.HEATING_TOO_FAST,indexToCellNo.get(i)-1);
                foundHeating = true;
            }
            if (temperatures[i] > oldTemperatures[i] + maxChange*timeDifference) {
                setCriticalStatusInCell(ErrorMessage.COOLING_TOO_FAST,indexToCellNo.get(i)-1);
                foundCooling = true;
            }
        }

        //add to general error list once
        if(foundHeating)
            setCritialStatus(ErrorMessage.HEATING_TOO_FAST);
        if(foundCooling)
            setCritialStatus(ErrorMessage.COOLING_TOO_FAST);
    }

}

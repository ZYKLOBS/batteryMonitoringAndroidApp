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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.msbattery.batterymonitoringapp.R;

public class Segment {
    private CID[] cids;
    private Status status;
    private ArrayList<String> statusMessages;
    private ArrayList<String> statusMessagesDetailed;

    public Segment(CID cid1, CID cid2) {
        cids = new CID[2];
        cids[0] = cid1;
        cids[1] = cid2;
    }

    public void setCID(int index, CID cid) {
        this.cids[index] = cid;
    }

    public CID getCID(int index) {
        return cids[index];
    }

    public Status getStatus() {
        if(status == null)
            status = ((cids[0].getStatus().v() > cids[1].getStatus().v()) ? cids[0].getStatus() : cids[1].getStatus());
        return status;
    }

    public float getMinVoltage(){return Math.min(cids[0].getMinPlausibleVoltage(),cids[1].getMinPlausibleVoltage());}
    public float getMedVoltage(){
        float[] v0 = cids[0].getVoltages();
        float[] v1 = cids[1].getVoltages();
        float[] voltages = Arrays.copyOf(v0,v0.length+v1.length);
        System.arraycopy(v1,0, voltages, v0.length,v1.length);

        return getMed(voltages);
    }
    public float getMaxVoltage(){return Math.max(cids[0].getMaxPlausibleVoltage(),cids[1].getMaxPlausibleVoltage());}

    public float getMinTemp(){return Math.min(cids[0].getMinTempNoUnplausible(),cids[1].getMinTempNoUnplausible());}
    public float getMedTemp(){
        float[] t0 = cids[0].getTemperatures();
        float[] t1 = cids[1].getTemperatures();
        float[] temps = Arrays.copyOf(t0,t0.length+t1.length);
        System.arraycopy(t1,0, temps, t0.length,t1.length);

        return getMed(temps);
    }
    public float getMaxTemp(){return Math.max(cids[0].getMaxTempNoUnplausible(),cids[1].getMaxTempNoUnplausible());}

    private float getMed(float[] array){
        float[] aCopy = array.clone();
        Arrays.sort(aCopy);
        if (aCopy.length%2==1){
            return aCopy[(int)(aCopy.length/2)];
        }
        return (aCopy[aCopy.length/2 -1] + aCopy[(int)(aCopy.length/2)]) /2;
    }


    // Meh, I dont know if I like this string building
    // TODO: Reconsider building string topdown

    public ArrayList<String> getStatusMessages() {

        if(statusMessages == null) {
            ArrayList<String> statusMessages = new ArrayList<>();

            for(String message : cids[0].getStatusMessages()) {
                statusMessages.add("A" + message);
            }
            for(String message : cids[1].getStatusMessages()) {
                statusMessages.add("B" + message);
            }
            this.statusMessages = statusMessages;

            if(this.getStatus().v() == Status.GOOD.v())
                this.statusMessages.add("GOOD");
        }
        if(this.statusMessages.isEmpty())
            this.statusMessages.add("GOOD");
        return this.statusMessages;
    }

    public ArrayList<String> getStatusMessagesDetailed() {
        if(statusMessagesDetailed == null) {
            ArrayList<String> statusMessagesDetailed = new ArrayList<>();

            for(String message : cids[0].getStatusMessagesDetailed()) {
                statusMessagesDetailed.add("A" + message);
            }
            for(String message : cids[1].getStatusMessagesDetailed()) {
                statusMessagesDetailed.add("B" + message);
            }
            this.statusMessagesDetailed = statusMessagesDetailed;
        }
        return this.statusMessagesDetailed;
    }

}

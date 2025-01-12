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

import java.time.Duration;
import java.time.Instant;

import de.msbattery.batterymonitoringapp.views.DetailedViewState;

public class OldBattery {
    private static OldBattery instance;
    private final float maxVoltageChange;
    private final float maxTempChange;
    private java.time.Instant oldTime;
    private java.time.Instant newTime;



    private Battery battery;

    public static OldBattery getInstance() {
        if (instance == null) {
            instance = new OldBattery();
        }
        return instance;
    }
    private OldBattery(){
        this.maxVoltageChange = 1; //TODO insert real values
        this.maxTempChange = 1; //TODO insert real values
    }
    public void setBattery(Battery battery) {
        this.battery = battery;
        oldTime = newTime;
        newTime = null;
    }

    public void setTime(){
        if (newTime == null) newTime = java.time.Instant.now();
    }
    public Duration getTimeDifference() {
        return Duration.between(oldTime,newTime);
    }

    public Segment getSegment(int index) {
        return battery.getSegment(index);
    }

    public float getMaxVoltageChange() {
        return maxVoltageChange;
    }

    public float getMaxTempChange() {
        return maxTempChange;
    }

    public boolean hasBattery(){return !(this.battery == null);}
}



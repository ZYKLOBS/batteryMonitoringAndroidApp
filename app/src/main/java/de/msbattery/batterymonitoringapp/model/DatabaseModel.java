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
import androidx.lifecycle.LiveData;

import de.msbattery.batterymonitoringapp.parser.DataParser;
import de.msbattery.batterymonitoringapp.structures.Battery;
import de.msbattery.batterymonitoringapp.structures.Segment;

public class DatabaseModel {


    float[][] voltValueList;
    float[][] tempValueList;

    float[] voltMinList;
    float[] voltMaxList;
    float[] voltMeanList;

    float[] tempMinList;
    float[] tempMaxList;
    float[] tempMeanList;

    public void setVoltValueList(float[][] voltValueList) {
        this.voltValueList = voltValueList;
    }

    public void setTempValueList(float[][] tempValueList) {
        this.tempValueList = tempValueList;
    }

    public void setVoltMinList(float[] voltMinList) {
        this.voltMinList = voltMinList;
    }

    public void setVoltMaxList(float[] voltMaxList) {
        this.voltMaxList = voltMaxList;
    }

    public void setVoltMeanList(float[] voltMeanList) {
        this.voltMeanList = voltMeanList;
    }

    public void setTempMinList(float[] tempMinList) {
        this.tempMinList = tempMinList;
    }

    public void setTempMaxList(float[] tempMaxList) {
        this.tempMaxList = tempMaxList;
    }

    public void setTempMeanList(float[] tempMeanList) {
        this.tempMeanList = tempMeanList;
    }


    public float getVoltMinList(int segIndex) {
        if (voltMinList != null)
            return voltMinList[segIndex];
        return 0;
    }

    public float getVoltMax(int segIndex) {
        if (voltMaxList != null)
            return voltMaxList[segIndex];
        return 0;
    }

    public float getVoltMean(int segIndex) {
        if (voltMeanList != null)
            return voltMeanList[segIndex];
        return 0;
    }

    public float getTempMin(int segIndex) {
        if (tempMinList != null)
            return tempMinList[segIndex];
        return 0;
    }

    public float getTempMax(int segIndex) {
        if (tempMaxList != null)
            return tempMaxList[segIndex];
        return 0;
    }

    public float getTempMean(int segIndex) {
        if (tempMeanList != null)
            return tempMeanList[segIndex];
        return 0;
    }
}

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

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SegmentHistory {
    //to do -> create java class that creates a history model each time that the app is opened and then every hour while the app is open
    private int id;
    private int segment;
    private String date;
    private float temperature_mean;

    private float voltage_mean;


    private float temperature_max;
    private float temperature_min;

    private float voltage_max;
    private float voltage_min;


    //constructor that should be used
    public SegmentHistory(int id, int segment, String date, float temperature_max, float temperature_min,
                          float temperature_mean, float voltage_max, float voltage_min, float voltage_mean) {
        this.id = id;
        this.segment = segment;
        this.date = date;
        this.temperature_max = temperature_max;
        this.temperature_min = temperature_min;
        this.temperature_mean = temperature_mean;
        this.voltage_max = voltage_max;
        this.voltage_min = voltage_min;
        this.voltage_mean = voltage_mean;
    }

    //in case non parametrized constructor needs to be used (hopefully not)
    public SegmentHistory() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSegment() {
        return segment;
    }

    public void setSegment(int segment) {
        this.segment = segment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public float getTemperature_max() {
        return temperature_max;
    }

    public void setTemperature_max(float temperature_max) {
        this.temperature_max = temperature_max;
    }

    public float getTemperature_min() {
        return temperature_min;
    }

    public void setTemperature_min(float temperature_min) {
        this.temperature_min = temperature_min;
    }

    public float getVoltage_max() {
        return voltage_max;
    }

    public void setVoltage_max(float voltage_max) {
        this.voltage_max = voltage_max;
    }

    public float getVoltage_min() {
        return voltage_min;
    }

    public void setVoltage_min(float voltage_min) {
        this.voltage_min = voltage_min;
    }


    public float getTemperature_mean() {
        return temperature_mean;
    }

    public void setTemperature_mean(float temperature_mean) {
        this.temperature_mean = temperature_mean;
    }

    public float getVoltage_mean() {
        return voltage_mean;
    }

    public void setVoltage_mean(float voltage_mean) {
        this.voltage_mean = voltage_mean;
    }


    @Override
    public String toString() {
        return "HistoryModel:" +
                "\n\tid=" + id +
                "\n\tsegment=" + segment +
                "\n\tdate=" + date +
                "\n\ttemperature (max/min)=" + temperature_max + " / " + temperature_min +
                "\n\ttemperature (mean)=" + temperature_mean +
                "\n\tvoltage (max/min)=" + voltage_max + " / " + voltage_min +
                "\n\tvoltage (mean)=" + voltage_mean;
    }
}
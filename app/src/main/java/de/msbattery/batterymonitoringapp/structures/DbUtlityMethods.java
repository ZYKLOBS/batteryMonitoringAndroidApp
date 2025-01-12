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

import java.text.DecimalFormat;

public class DbUtlityMethods {
    public static float roundToTwoDecimals(float value) {
        return Math.round(value*100f)/100f;
    }

    public static float getMinTemp(float[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }

        // Initialize the minimum value to the largest possible float value
        float min = Float.MAX_VALUE;
        boolean validValueFound = false;

        // Find the minimum value in the array
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= 0.1 || values[i] <= -0.1) { // Exclude values close to zero
                if (values[i] < min) {
                    min = values[i];
                    validValueFound = true;
                }
            }
        }

        if (!validValueFound) {
            return -1000; //error value, no min found
        }

        // Return the minimum value
        return min;
    }

    public static float getMeanTemp(float[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }

        float sum = 0;
        int validElements = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] >= 0.1 || values[i] <= -0.1) { // Exclude values close to zero
                sum += values[i];
                validElements++;
            }
        }

        if (validElements == 0) {
            return -1000;//error value, could not compute average
        }

        return sum / validElements;
    }


    public static float getMax(float[] values) {
        float max = values[0];  // Initialize max with the first element

        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }
    public static float getMin(float[] values) {
        float min = values[0];  // Initialize min with the first element


        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }

    public static float getMean(float[] values) {
        float sum = 0;
        int entries = 0;
        for (float value:values){
            sum += value;
            entries++;
        }
        return sum/entries;
    }
}

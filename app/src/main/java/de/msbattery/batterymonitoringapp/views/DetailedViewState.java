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

package de.msbattery.batterymonitoringapp.views;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class DetailedViewState
{
    private static DetailedViewState instance;
    private final int segmentCount;
    private final MutableLiveData<Integer> currentState = new MutableLiveData<>(); //Where state is the current Segment beginning from 1

    public static synchronized DetailedViewState getInstance() {
        if (instance == null) {
            instance = new DetailedViewState();
        }
        return instance;
    }
    private DetailedViewState() {
        this.segmentCount = 12;

        this.currentState.setValue(1);
    }

    public int getState()
    {
        return this.currentState.getValue();
    }

    public LiveData<Integer> getStateLiveData() {
        return this.currentState;
    }

    public int setState(int state) {
        // Info: Java returns negative number after -x % 12.
        int r = (state -1) % segmentCount;
        if (r < 0)
            r += segmentCount;
        this.currentState.postValue(r + 1);
        return r+1;
    }
}

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

package de.msbattery.batterymonitoringapp.database;

import android.os.Handler;
import android.os.Looper;
public class TimerManager {
    private static TimerManager instance;
    private long startTime;
    private long elapsedTime;
    private boolean isRunning;
    private TimerListener listener;
    private Handler handler;
    private Runnable timerRunnable;
    private long updateInterval = 1000; // Default update interval of 10 seconds, i.e. timer checks if time passed
    private long finishInterval = 1200000;//20 minutes

    public interface TimerListener {
        void onTick(long elapsedTime);
        void onTimerFinished();
    }

    private TimerManager() {
        handler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    if (listener != null) {
                        listener.onTick(elapsedTime);
                    }
                    if (elapsedTime >= finishInterval) {
                        listener.onTimerFinished();
                        elapsedTime = 0;
                        startTime = System.currentTimeMillis();
                    }
                    handler.postDelayed(this, updateInterval); // Use the configurable interval
                }
            }
        };
    }

    public static TimerManager getInstance() {
        if (instance == null) {
            instance = new TimerManager();
        }
        return instance;
    }

    public void setUpdateInterval(long interval) {
        this.updateInterval = interval;
    }

    public void startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            isRunning = true;
            handler.post(timerRunnable);
        }
    }

    public void stopTimer() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(timerRunnable);
        }
    }

    public void resetTimer() {
        stopTimer();
        elapsedTime = 0;
        if (listener != null) {
            listener.onTick(elapsedTime);
        }
    }

    public void setFinishInterval(long finishInterval)
    {
        this.finishInterval = finishInterval;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setTimerListener(TimerListener listener) {
        this.listener = listener;
    }
}
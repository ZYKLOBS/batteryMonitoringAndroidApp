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

package de.msbattery.batterymonitoringapp;

import static de.msbattery.batterymonitoringapp.structures.DbUtlityMethods.getMax;
import static de.msbattery.batterymonitoringapp.structures.DbUtlityMethods.getMean;
import static de.msbattery.batterymonitoringapp.structures.DbUtlityMethods.getMeanTemp;
import static de.msbattery.batterymonitoringapp.structures.DbUtlityMethods.getMin;
import static de.msbattery.batterymonitoringapp.structures.DbUtlityMethods.getMinTemp;
import static de.msbattery.batterymonitoringapp.structures.DbUtlityMethods.roundToTwoDecimals;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


import de.msbattery.batterymonitoringapp.connection.WebSocketClient;
import de.msbattery.batterymonitoringapp.deviceUtils.DeviceUtils;
import de.msbattery.batterymonitoringapp.model.DatabaseModel;
import de.msbattery.batterymonitoringapp.model.DatabaseViewModel;
import de.msbattery.batterymonitoringapp.views.DetailedViewState;
import de.msbattery.batterymonitoringapp.views.SimpleView;


import android.widget.Toast;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import de.msbattery.batterymonitoringapp.database.DataBaseHelper;
import de.msbattery.batterymonitoringapp.database.TimerManager;
import de.msbattery.batterymonitoringapp.model.DetailedModel;
import de.msbattery.batterymonitoringapp.model.DetailedViewModel;

import de.msbattery.batterymonitoringapp.structures.SegmentHistory;


public class MainActivity extends AppCompatActivity  {
    // RUB-Motorsports Group 5
    private DetailedViewState detailedViewState = DetailedViewState.getInstance();
    private int cellCount = 22;
    private int segmentCount = 12;

    private TimerManager timerManager;

    private DatabaseViewModel dbViewModel;
    private boolean timer_finished = false;



    private void insertSegmentToDB(DatabaseModel databaseModel) {

        //Toast.makeText(getActivity(), "updating database...", Toast.LENGTH_SHORT).show();
        float[] voltages = new float[cellCount];
        float[] temps = new float[cellCount];
        float[] volt_maxima = new float[segmentCount];
        float[] volt_minima = new float[segmentCount];
        float[] volt_mean = new float[segmentCount];
        float[] temps_maxima = new float[segmentCount];
        float[] temps_minima = new float[segmentCount];
        float[] temps_mean = new float[segmentCount];


        for (int segmentNumber = 1; segmentNumber <segmentCount + 1;segmentNumber++){

            //TODO: ein Beispiel, wie's gehen sollte
            Log.d("temps_mean", "" + databaseModel.getTempMean(segmentNumber - 1) + "");
            temps_maxima[segmentNumber-1] = roundToTwoDecimals(databaseModel.getTempMax(segmentNumber - 1));
            temps_minima[segmentNumber-1] = roundToTwoDecimals(databaseModel.getTempMin(segmentNumber - 1));
            temps_mean[segmentNumber-1] = roundToTwoDecimals(databaseModel.getTempMean(segmentNumber - 1));

            volt_maxima[segmentNumber-1] = roundToTwoDecimals(databaseModel.getVoltMax(segmentNumber - 1));
            volt_minima[segmentNumber-1] = roundToTwoDecimals(databaseModel.getVoltMinList(segmentNumber - 1));
            volt_mean[segmentNumber-1] = roundToTwoDecimals(databaseModel.getVoltMean(segmentNumber - 1));

        }
        SegmentHistory segmentHistory;
        DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);

        //Log.d("testSelect", dataBaseHelper.select_last_n_segment(5).get(0).toString());
        boolean entry_debugger = true;
        for (int segmentNumber = 0; segmentNumber <segmentCount;segmentNumber++) {

            try {

                segmentHistory = new SegmentHistory(-1, segmentNumber+1, SegmentHistory.getCurrentDate(), temps_maxima[segmentNumber],

                        temps_minima[segmentNumber], temps_mean[segmentNumber],
                        volt_maxima[segmentNumber], volt_minima[segmentNumber],
                        volt_mean[segmentNumber]);
                //Log.d("segment_Creation", segmentHistory.toString());
            }
            catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error Creating SegmentHistoryModel", Toast.LENGTH_SHORT).show();
                segmentHistory = new SegmentHistory(-1, -1, "error", -1, -1, -1, -1, -1, -1);//default error value
            }

            boolean successfulEntry = dataBaseHelper.insert_segment(segmentHistory);
            if(successfulEntry)
            {
                //Toast.makeText(MainActivity.this, "Successful entry", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //Toast.makeText(MainActivity.this, "Failed To insert into database", Toast.LENGTH_SHORT).show();
                entry_debugger = false;
            }


        }
        if (!entry_debugger) //atleast one segment failed to insert
        {
            Toast.makeText(MainActivity.this, "Failed To save to database", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(MainActivity.this, "Successfully saved to database", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Force portrait mode if the device is not a tablet
        if (!DeviceUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if(dbViewModel == null) {
            dbViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SimpleView()).commit();


        timerManager = TimerManager.getInstance();
        timerManager.setTimerListener(new TimerManager.TimerListener() {
            @Override
            public void onTick(long elapsedTime) {
                //Can stay empty, runs every tick, i.e. 10 seconds
            }

            @Override
            public void onTimerFinished() {
                timer_finished = true;
                timerManager.setFinishInterval(1200000); //20 minutes in ms
                // Restart the timer to loop it
                timerManager.startTimer();

            }

        });

        //This line basically means you have to have the app open for 10000 ms for it to make a database entry, after that it stores data every 20 minutes the app is open
        timerManager.setFinishInterval(10000); //potential bug source <-- if no data is available within 10 secs
        timerManager.startTimer(); // Start the timer initially




        dbViewModel.getData().observe(MainActivity.this, new Observer<DatabaseModel>() {
            @Override
            public void onChanged(DatabaseModel databaseModel) {

                if (!timer_finished) {
                    return;
                }
                if (databaseModel == null) {
                    //TLog.d("detailedViewModel", "Detailed Data: " + detailedModel.getTemp(0) + "tmp\n" + detailedModel.getVoltage(0) + "v");
                    return;
                }
                timer_finished = false;
                insertSegmentToDB(databaseModel);

            }
        });

    }


}
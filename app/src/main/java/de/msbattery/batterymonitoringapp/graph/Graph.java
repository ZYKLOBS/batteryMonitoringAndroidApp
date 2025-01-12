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

package de.msbattery.batterymonitoringapp.graph;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.msbattery.batterymonitoringapp.R;
import de.msbattery.batterymonitoringapp.database.DataBaseHelper;
import de.msbattery.batterymonitoringapp.structures.SegmentHistory;

public class Graph {
    private LineChart[] charts;

    private final View view;

    public Graph(@NonNull View _view, int state){
        view = _view;
        addGraphs(state);
    }

    private void addGraphs(int state){

        configureCharts();

        //Setting Linedatasets
        LineDataSet lds1 = new LineDataSet(new ArrayList<>(),"Avg. Temperature");
        lds1.setColor(Color.parseColor("#40BF40"));
        configureLDS(lds1);
        LineDataSet lds2 = new LineDataSet(new ArrayList<>(),"Max. Temperature");
        lds2.setColor(Color.parseColor("#FF8888"));
        configureLDS(lds2);
        LineDataSet lds3 = new LineDataSet(new ArrayList<>(),"Min. Temperature");
        lds3.setColor(Color.parseColor("#880000"));
        configureLDS(lds3);
        LineDataSet lds4 = new LineDataSet(new ArrayList<>(),"Avg. Voltage");
        lds4.setColor(Color.parseColor("#BFBF40"));
        configureLDS(lds4);
        LineDataSet lds5 = new LineDataSet(new ArrayList<>(),"Max. Voltage");
        lds5.setColor(Color.parseColor("#88FFFF"));
        configureLDS(lds5);
        LineDataSet lds6 = new LineDataSet(new ArrayList<>(),"Min. Voltage");
        lds6.setColor(Color.parseColor("#008888"));
        configureLDS(lds6);

        ArrayList<ILineDataSet> dataSets1 = new ArrayList<>();
        dataSets1.add(lds3);
        dataSets1.add(lds1);
        dataSets1.add(lds2);
        ArrayList<ILineDataSet> dataSets2 = new ArrayList<>();
        dataSets2.add(lds6);
        dataSets2.add(lds4);
        dataSets2.add(lds5);

        LineData lData1 = new LineData(dataSets1);
        LineData lData2 = new LineData(dataSets2);

        try{
            DataBaseHelper dbh = new DataBaseHelper(view.getContext());
            List<SegmentHistory> shlist = dbh.select_last_n_segment(15, state);
            //Prevent Crash on empty list
            if(shlist.isEmpty())return;
            charts[0].setData(lData1);
            charts[1].setData(lData2);
            String lastDate = shlist.get(shlist.size()-1).getDate();
            String newestDate = shlist.get(0).getDate();

            //Add Entries
            for(int i = 0; i < shlist.size(); i++){
                SegmentHistory sh = shlist.get(shlist.size()-i-1);

                //Compute Time Difference in Seconds
                int deltaT = calculate_delta_t_in_seconds(lastDate, sh.getDate());

                //Don't display very old values (12 hours ago is roughly very old)
                if(calculate_delta_t_in_seconds(newestDate, sh.getDate()) < -50000)continue;
                float minTemp = sh.getTemperature_min();
                float avgTemp = sh.getTemperature_mean();
                float maxTemp = sh.getTemperature_max();
                float minVolt = sh.getVoltage_min();
                float avgVolt = sh.getVoltage_mean();
                float maxVolt = sh.getVoltage_max();
                appendValuesToGraph(charts[1], lds6, lds4, lds5, minVolt, avgVolt, maxVolt, deltaT);
                //Dont Display faulty values
                if(minTemp < - 20)continue;
                appendValuesToGraph(charts[0], lds3, lds1, lds2, minTemp, avgTemp, maxTemp, deltaT);
            }

            //Do this to prevent the app crashing
            if(lds1.getEntryCount() == 0 || lds2.getEntryCount() == 0 || lds3.getEntryCount() == 0){
                appendValuesToGraph(charts[0], lds3, lds1, lds2, 0, 0, 0, 0);
            }

        } catch(Exception e){
            Log.e("DB-ERROR", "Problem Accessing Database", e);
       }
    }

    //Might be possible to access the linedatasets from the graph so you wouldn't have to pass them as parameters
    private  void appendValuesToGraph(LineChart graph, LineDataSet setLow, LineDataSet setAvg, LineDataSet setHi, float low, float avg, float hi, int count){
        setLow.addEntry(new Entry(count, low));
        setAvg.addEntry(new Entry(count, avg));
        setHi.addEntry(new Entry(count,hi));
        graph.getData().notifyDataChanged();
        graph.notifyDataSetChanged();
        graph.invalidate();
    }

    private void configureCharts(){
        charts = new LineChart[2];
        charts[0] = view.findViewById(R.id.chart1);
        charts[1] = view.findViewById(R.id.chart2);

        //Setting Colors
        charts[0].getLegend().setTextColor(Color.WHITE);
        charts[1].getLegend().setTextColor(Color.WHITE);
        charts[0].getLegend().setTextSize(13f);
        charts[1].getLegend().setTextSize(13f);
        charts[0].getAxisLeft().setTextColor(Color.WHITE);
        charts[1].getAxisLeft().setTextColor(Color.WHITE);
        charts[0].getAxisRight().setTextColor(Color.WHITE);
        charts[1].getAxisRight().setTextColor(Color.WHITE);
        charts[0].getXAxis().setEnabled(false);
        charts[1].getXAxis().setEnabled(false);
        charts[0].getDescription().setEnabled(false);
        charts[1].getDescription().setEnabled(false);
        charts[0].fitScreen();
        charts[1].fitScreen();
    }

    private void configureLDS(LineDataSet lds){
        lds.setCircleRadius(2f);
        lds.setLineWidth(2f);
        lds.setDrawValues(false);
    }

    //Calculate the difference between two dates in minutes
    private static int calculate_delta_t_in_seconds(String time1, String time2) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = format.parse(time1);
        Date date2 = format.parse(time2);
        // Calculate the difference in milliseconds
        long differenceInMillis = date2.getTime() - date1.getTime();

        // Convert the difference from milliseconds to minutes
        return (int)TimeUnit.MILLISECONDS.toSeconds(differenceInMillis);
    }

    public void reload(int state){
        addGraphs(state);
    }
}
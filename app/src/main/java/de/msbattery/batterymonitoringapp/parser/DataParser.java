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

package de.msbattery.batterymonitoringapp.parser;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.LinkedList;
import java.util.ArrayList;

import de.msbattery.batterymonitoringapp.connection.WebSocketClient;
import de.msbattery.batterymonitoringapp.structures.Battery;
import de.msbattery.batterymonitoringapp.structures.CID;
import de.msbattery.batterymonitoringapp.structures.OldBattery;
import de.msbattery.batterymonitoringapp.structures.Segment;

public class DataParser {

    private static DataParser instance;
    private LinkedList<DataBlock> currentData = new LinkedList<>();
    private final MutableLiveData<Battery> batteryLiveData = new MutableLiveData<>();

    //Constructor
    private DataParser(){
        WebSocketClient.getInstance().addListener(this::parseMessage);
    }

    public static synchronized DataParser getInstance() {
        if (instance == null) {
            instance = new DataParser();
        }
        return instance;
    }

    //Decodes base64 encoded string and stores result in List
    public void decodeBase64(String message){
        flush();
        byte[] bytes = Base64.getDecoder().decode(message);
        int blockNumber = bytes.length/4;
        for(int i = 0; i < blockNumber; i++){
            int pos = i*4;
            byte a = bytes[pos];
            byte b = bytes[pos+1];
            byte c = bytes[pos+2];
            byte d = bytes[pos+3];
            currentData.add(new DataBlock(a,b,c,d));
        }
    }

    private long computeChecksum(){
        long checksum = 0;

        for(int i = 0; i < this.getBlockCount()-1; i++){
            DataBlock block = this.getBlock(i);
            for (int j = 0; j < 4; j++) {
                checksum ^= ((long) Byte.toUnsignedInt(block.getAt(j)) << ((i*4+j) % 24));
            }
        }
        return checksum;
    }

    private float blockToFloat(DataBlock block) {
        byte[] bytes = {block.getAt(3), block.getAt(2), block.getAt(1), block.getAt(0)};
        return ByteBuffer.wrap(bytes).getFloat();
    }

    // ByteBuffer.warp turns this 0x59020000
    // Into this 0x00000259

    private int blockToInt32(DataBlock block) {
        byte[] bytes = {block.getAt(3), block.getAt(2), block.getAt(1), block.getAt(0)};
        return ByteBuffer.wrap(bytes).getInt();
    }
    private long blockToLong(DataBlock block) {
        byte[] bytes = {0, 0, 0, 0, block.getAt(3), block.getAt(2), block.getAt(1), block.getAt(0)};
        return ByteBuffer.wrap(bytes).getLong();
    }

    // This reads 2 shorts !
    // Too bad, there is no unsigned in Java :(
    // TODO: Check if this is in correct order
    private short[] blockToInt(DataBlock block) {
        byte[] bytes1 = {block.getAt(3), block.getAt(2)};
        byte[] bytes2 = {block.getAt(1), block.getAt(0)};

        return new short[]{ByteBuffer.wrap(bytes2).getShort(), ByteBuffer.wrap(bytes1).getShort()};
    }

    //dekodiert jeden Datablock des aktuellen Frames(currentData)
    public void parseMessage(String message) {

        this.decodeBase64(message);

        int length = blockToInt32(this.getBlock(0));
        int globPos = 1;

        ArrayList<Segment> segments = new ArrayList<>();
        boolean newSegment = true;

        OldBattery oldBattery = OldBattery.getInstance();
        oldBattery.setTime();

        while(globPos < length-1) {
            float[] voltages = new float[11];
            for(int i = 0; i < 11; i++, globPos++)
                voltages[i] = (blockToFloat(this.getBlock(globPos)));

            globPos++; //discard first temperature value as it is always incorrect
            float[] stackTemps = new float[6];
            for(int i = 0; i < 6; i++, globPos++)
                stackTemps[i] = (blockToFloat(this.getBlock(globPos)));

            float stackVoltage = blockToFloat(this.getBlock(globPos++));
            float icTemperature = blockToFloat(this.getBlock(globPos++));

            short[] masterFaultReg = new short[3];

            // Note: result is only for temporarily saving values
            short[] result = blockToInt(this.getBlock(globPos++));
            masterFaultReg[0] = result[0];
            masterFaultReg[1] = result[1];

            short[] secFaultReg = new short[6];
            result = blockToInt(this.getBlock(globPos++));
            masterFaultReg[2] = result[0];
            secFaultReg[0] = result[1];

            result = blockToInt(this.getBlock(globPos++));
            secFaultReg[1] = result[0];
            secFaultReg[2] = result[1];

            result = blockToInt(this.getBlock(globPos++));
            secFaultReg[3] = result[0];
            secFaultReg[4] = result[1];

            result = blockToInt(this.getBlock(globPos++));
            secFaultReg[5] = result[0];
            // Second element is always 0

            CID cid;
            if (oldBattery.hasBattery()) {
                Segment oldSegment = oldBattery.getSegment(segments.size()-(newSegment ? 0 : 1));
                CID oldCID = oldSegment.getCID(newSegment ? 0 : 1);
                float[] oldVoltages = oldCID.getVoltages();
                float[] oldTemperatures = oldCID.getTemperatures();

                cid = new CID(voltages, stackTemps, stackVoltage, icTemperature, masterFaultReg, secFaultReg, oldVoltages, oldTemperatures);
            }
            else {
                cid = new CID(voltages, stackTemps, stackVoltage, icTemperature, masterFaultReg, secFaultReg, null, null);
            }

            if(newSegment)
                segments.add(new Segment(cid, null));
            else
                segments.get(segments.size() - 1).setCID(1, cid);

            newSegment = !newSegment;
        }
        Battery battery = new Battery();
        battery.setConnected(WebSocketClient.getInstance().getOpened());
        battery.setSegmentList(segments);

        //checksum is the last block in each received message
        if (computeChecksum() == blockToLong((this.getBlock(length))))
            batteryLiveData.postValue(battery);

        oldBattery.setBattery(battery);
        //Log.i("OldBattery", "new old battery set");

    }

    public LiveData<Battery> getBatteryLiveData() {
        return batteryLiveData;
    }

    //Empties List of Blocks
    public void flush(){
        currentData = new LinkedList<>();
    }

    public DataBlock getBlock(int index){
        return currentData.get(index);
    }

    public int getBlockCount(){
        return currentData.size();
    }
}



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

import java.util.ArrayList;

public class Battery {
    private ArrayList<Segment> segmentList = new ArrayList<>();

    private Status overallStatus;
    private boolean connected = false;

    public void setSegmentList(ArrayList<Segment> segmentList) {
        this.segmentList = segmentList;
    }

    public Segment getSegment(int index){return segmentList.get(index);}

    public String getErrorReportsAsString() { //errors in cells (statusMessagesDetailed) as well as errors in master fault registers (statusMessages)
        if(segmentList == null) {
            return ErrorMessage.STATUS_ERROR.getMessage();
        }
        StringBuilder errorString = new StringBuilder();
        for (int i = 0; i < segmentList.size(); i++) {
            Segment segment = segmentList.get(i);
            String listString = segment.getStatusMessages().toString().concat(segment.getStatusMessagesDetailed().toString());
            errorString.append("Segment ").append(i).append(": ").append(listString.substring(1, listString.length() - 1)).append("\n");
        }
        return errorString.toString();
    }


    // puts the error-messages in ascending segment order, a entry is either "[GOOD]" or the error message (not sure if this belongs here)
    public ArrayList<String> getErrorReport() {
        ArrayList<String> errorList = new ArrayList<>();
        if(segmentList == null) {
            errorList.add(ErrorMessage.STATUS_ERROR.getMessage());
            return errorList;
        }

        for (int i = 0; i < segmentList.size(); i++) {      // i=0 means Segment 1

            /*if(i==4 || i==9){                               // error in Segment 5 + 10 for testing/demonstration,
                errorList.add("This is a Error-message.");    // just delete the multi-line comment :)
                continue;
            }*/

            errorList.add(segmentList.get(i).getStatusMessages().toString());
        }
        return errorList;
    }


    public Status getOverallStatus() {
        /*if (Math.random() > 0.2) return Status.GOOD;
        else return Status.CRITICAL;*/
        if (overallStatus == null)
            determineStatus();
        return this.overallStatus;
    }

    private void determineStatus() {
        if (segmentList == null)
            this.overallStatus = Status.FAILURE;
        else {
            this.overallStatus = Status.GOOD;
            for (Segment segment: segmentList) {
                if (segment.getStatus().v() > this.overallStatus.v()) {
                    this.overallStatus = segment.getStatus();
                }
            }
        }
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

}

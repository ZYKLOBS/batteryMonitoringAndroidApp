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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;



import de.msbattery.batterymonitoringapp.structures.EmergencyContact;
import de.msbattery.batterymonitoringapp.structures.SegmentHistory;



//Everything related to the database is based on  https://youtu.be/312RhjfetP8
public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String segment_history = "segment_history";
    public static final String column_id = "ID";
    public static final String segment_number = "segment";
    public static final String column_temp_max = "temp_max";
    public static final String column_temp_min = "temp_min";
    public static final String column_volt_max = "volt_max";
    public static final String column_volt_min = "volt_min";
    public static final String segment_history_table = "segment_history";

    public static final String emergency_contact_table = "emergency_contacts";
    public static final String date = "date";
    public static final String name = "name";
    public static final String phone_number = "phone_number";
    public static final String email_address = "email_address";
    public static final String is_displayed = "is_displayed";
    public static final String config_table = "config_data";
    public static final String config_key = "config_key";
    public static final String config_value = "config_value";
    public static final String column_temp_mean = "temp_mean";
    public static final String column_volt_mean = "volt_mean";




    public DataBaseHelper(@Nullable Context context) {
        super(context, "segment_history.db", null, 1); //factory can be null for default
    }

    //will be called the first time we try to access the database -> create db or load db if exists
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSegmentTableStatement = "CREATE TABLE " + segment_history_table + " ("+
                column_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                segment_number + " INTEGER, " +
                date + " TEXT, " +
                column_temp_max + " REAL, " + //real has 7 digits of precision, should be sufficient
                column_temp_min + " REAL, " +
                column_temp_mean + " REAL, " +
                column_volt_max + " REAL, " +
                column_volt_min + " REAL, " +
                column_volt_mean + " REAL)";


        String createEmergencyContactsTable = "CREATE TABLE " + emergency_contact_table + " ("+
                column_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                name + " TEXT UNIQUE, " +
                phone_number + " TEXT, " +
                email_address + " TEXT, " +
                is_displayed + " INTEGER)"; //1 if yes, 0 if no

        String createConfigTable = "CREATE TABLE " + config_table + " (" +
                config_key + " TEXT PRIMARY KEY, " +
                config_value + " TEXT)";



        db.execSQL(createConfigTable);
        db.execSQL(createSegmentTableStatement);
        db.execSQL(createEmergencyContactsTable);
        insert_default_config(db);
    }

    //use if database version number changes -> modify schema automatically #TODO
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insert_default_config(SQLiteDatabase db){
        // Insert default password configuration
        ContentValues passwordCV = new ContentValues();
        passwordCV.put(config_key, "password");
        passwordCV.put(config_value, "1234");

        long passwordResult = db.insertWithOnConflict(config_table, null, passwordCV, SQLiteDatabase.CONFLICT_IGNORE);

        // Insert default SMS notifications configuration
        ContentValues smsNotificationsCV = new ContentValues();
        smsNotificationsCV.put(config_key, "smsNotifications");
        smsNotificationsCV.put(config_value, "1");  // 1 := SMS Notifications on, 0 := SMS Notifications off

        long smsNotificationsResult = db.insertWithOnConflict(config_table, null, smsNotificationsCV, SQLiteDatabase.CONFLICT_IGNORE);

        // Insert default email notifications configuration
        ContentValues emailNotificationsCV = new ContentValues();
        emailNotificationsCV.put(config_key, "emailNotifications");
        emailNotificationsCV.put(config_value, "1");  // 1 := Email Notifications on, 0 := Email Notifications off

        long emailNotificationsResult = db.insertWithOnConflict(config_table, null, emailNotificationsCV, SQLiteDatabase.CONFLICT_IGNORE);

        if (passwordResult == -1 || smsNotificationsResult == -1 || emailNotificationsResult == -1) {
            Log.d("insert_default_config", "Default entry already exists or failed to insert.");
            return false;
        } else {
            Log.d("insert_default_config", "Default entries inserted successfully.");
            return true;
        }
    }
    public String select_password() {
        String queryString = "SELECT " + config_value + " FROM " + config_table + " WHERE " + config_key + " = 'password'";

        SQLiteDatabase db = this.getReadableDatabase();
        String resultString = null;

        Cursor result = db.rawQuery(queryString, null);

        if (result.moveToFirst()) {
            resultString = result.getString(0); //since select only returns config value column
        } else {
            // If no result is found, return error message
            resultString = "[error]";
        }

        result.close();
        db.close();

        return resultString;
    }
    public String select_smsNotifications() {
        String queryString = "SELECT " + config_value + " FROM " + config_table + " WHERE " + config_key + " = 'smsNotifications'";

        SQLiteDatabase db = this.getReadableDatabase();
        String resultString = null;

        Cursor result = db.rawQuery(queryString, null);

        if (result.moveToFirst()) {
            resultString = result.getString(0); //since select only returns config value column
        } else {
            // If no result is found, return error message
            resultString = "[error]";
        }

        result.close();
        db.close();

        return resultString;
    }

    public String select_emailNotifications() {
        String queryString = "SELECT " + config_value + " FROM " + config_table + " WHERE " + config_key + " = 'emailNotifications'";

        SQLiteDatabase db = this.getReadableDatabase();
        String resultString = null;

        Cursor result = db.rawQuery(queryString, null);

        if (result.moveToFirst()) {
            resultString = result.getString(0); //since select only returns config value column
        } else {
            // If no result is found, return error message
            resultString = "[error]";
        }

        result.close();
        db.close();

        return resultString;
    }


    public boolean turn_smsNotifications_off() {
        return update_smsNotifications("0");
    }

    public boolean turn_smsNotifications_on() {
        return update_smsNotifications("1");
    }

    public boolean turn_emailNotifications_off() {
        return update_emailNotifications("0");
    }

    public boolean turn_emailNotifications_on() {
        return update_emailNotifications("1");
    }
    public boolean update_smsNotifications(String newValue) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(config_value, newValue);

        int rowsAffected = db.update(config_table, cv, config_key + " = ?", new String[]{"smsNotifications"});
        db.close();

        return rowsAffected == 1; //should only update one row
    }

    public boolean update_emailNotifications(String newValue) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(config_value, newValue);

        int rowsAffected = db.update(config_table, cv, config_key + " = ?", new String[]{"emailNotifications"});
        db.close();

        return rowsAffected == 1; //should only update one row
    }


    public boolean updatePassword(String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(config_value, newPassword);

        int rowsAffected = db.update(config_table, cv, config_key + " = ?", new String[]{"password"}); //sql injection proof
        db.close();

        return rowsAffected == 1; //should only update one row
    }

    public boolean insert_emergency_contact(EmergencyContact emergencyContact){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Add values to content values
        cv.put(name, emergencyContact.getName());
        cv.put(phone_number, emergencyContact.getPhoneNumber());
        cv.put(email_address, emergencyContact.getEmail());
        cv.put(is_displayed, emergencyContact.getIs_displayed());

        try {
            long insert = db.insertOrThrow(emergency_contact_table, null, cv);
            if (insert == -1) {
                Log.e("INSERT_ERROR", "Failed to insert into database");
                return false;
            } else {
                Log.d("INSERT_SUCCESS", "Successfully inserted into database");
                return true;
            }
        } catch (SQLiteConstraintException e) { //notify user that name has to be unique
            Log.e("INSERT_ERROR", "Failed to insert into database due to unique constraint violation");
            return false;
        } finally {
            db.close();
        }
    }

    public boolean insert_segment(SegmentHistory segmentHistory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Add values to content values
        cv.put(segment_number, segmentHistory.getSegment());
        cv.put(column_temp_max, segmentHistory.getTemperature_max());
        cv.put(column_temp_min, segmentHistory.getTemperature_min());
        cv.put(column_temp_mean, segmentHistory.getTemperature_mean());
        cv.put(date, segmentHistory.getDate());
        cv.put(column_volt_max, segmentHistory.getVoltage_max());
        cv.put(column_volt_min, segmentHistory.getVoltage_min());
        cv.put(column_volt_mean, segmentHistory.getVoltage_mean());

        // Log the ContentValues
        Log.d("INSERT_DEBUG", "ContentValues: " + cv.toString());

        long insert = db.insert(segment_history_table, null, cv);
        db.close();

        if (insert == -1) {
            Log.e("INSERT_ERROR", "Failed to insert into database");
            return false;
        } else {
            Log.d("INSERT_SUCCESS", "Successfully inserted into database");
            return true;
        }
    }

    public List<EmergencyContact> select_emergency_contacts(){
        List<EmergencyContact> returnList = new ArrayList<EmergencyContact>();


        String queryString = "Select * FROM " + emergency_contact_table;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery(queryString, null);

        if (result.moveToFirst()) { //move to the first result if true then the operation delivered results
            do { //for order/indices please see table creation
                int contactId = result.getInt(0);
                String name = result.getString(1);
                String phone = result.getString(2);
                String email = result.getString(3);
                boolean isDisplayed = result.getInt(4) == 1 ? true: false;


                EmergencyContact currentEmergencyContact = new EmergencyContact(contactId,name, phone, email, isDisplayed);
                returnList.add(currentEmergencyContact);

            } while(result.moveToNext());
        }
        else { //no results from query, empty table

        }

        result.close();
        db.close();

        return returnList;
    }

    public boolean deleteEmergencyContactByName(String contactName) {
        SQLiteDatabase db = this.getWritableDatabase();


        int rowsAffected = db.delete(emergency_contact_table, name + " = ?", new String[]{contactName});

        db.close();

        if (rowsAffected > 0) {
            Log.d("DELETE_SUCCESS", "Successfully deleted contact with name: " + contactName);
            return true;
        } else {
            Log.e("DELETE_ERROR", "No contact found with name: " + contactName);
            return false;
        }
    }
    public boolean updateEmergencyContactDisplayStatusByName(String contactName, int displayStatus) { //1 for displayed, 0 for not displayed
        SQLiteDatabase db = this.getWritableDatabase();
        if (displayStatus <0 || displayStatus > 1)
        {
            Log.d("updateEmergencyContact", "Wrong value entered must be 1 or 0");
            return false;
        }

        ContentValues cv = new ContentValues();


        cv.put(is_displayed, displayStatus);

        // Update the `is_displayed` column for the contact with the unique name
        int rowsAffected = db.update(emergency_contact_table, cv, name + " = ?", new String[]{contactName});
        db.close();

        return rowsAffected == 1;
    }

    public List<SegmentHistory> select_last_n_segment(long n, int segmentIdx){ //index starts at 1 and ends at 12 (inclusive)
        List<SegmentHistory> returnList = new ArrayList<SegmentHistory>();
        if (n < 1) { //illegal value
            Log.d("select_last_n","Illegal value for n");
            return returnList;
        }

        String queryString = "SELECT * FROM " + segment_history_table + " WHERE " + segment_number + " = " + segmentIdx + " ORDER BY " + column_id + " DESC LIMIT " + n;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result   = db.rawQuery(queryString, null);

        if (result.moveToFirst()) { //move to the first result if true then the operation delivered results
            do { //for order/indices please see table creation
                int segmentID = result.getInt(0);
                int segmentNumber = result.getInt(1);
                String date = result.getString(2);
                float temp_max = result.getFloat(3);
                float temp_min = result.getFloat(4);
                float temp_mean = result.getFloat(5);
                float volt_max = result.getFloat(6);
                float volt_min = result.getFloat(7);
                float volt_mean = result.getFloat(8);
                n -= 1;

                SegmentHistory currentSegmentHistory = new SegmentHistory(segmentID, segmentNumber, date, temp_max,
                        temp_min, temp_mean, volt_max, volt_min, volt_mean);
                returnList.add(currentSegmentHistory);

            } while(result.moveToNext() && n > 0);
        }
        else { //no results from query, empty table
            Log.d("select_last_n", "No results found for segment number " + segmentIdx); //insteadofEmpty
        }

        result.close();
        db.close();

        return returnList;
    }



    public List<SegmentHistory> getRecordsInDateRange(String startDate, String endDate) { //user should choose this, if we dont provide

        //predetermined cleaned values -> sql injection lol | must be correct format yyyy-MM-dd

        List<SegmentHistory> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String queryString = "SELECT * FROM your_table WHERE date_column BETWEEN " + startDate + " AND " +endDate;

        Cursor result = db.rawQuery(queryString, null);

        if (result.moveToFirst()) {
            do {
                int segmentID = result.getInt(0);
                int segmentNumber = result.getInt(1);
                String date = result.getString(2);
                float temp_max = result.getFloat(3);
                float temp_min = result.getFloat(4);
                float temp_mean = result.getFloat(5);
                float volt_max = result.getFloat(6);
                float volt_min = result.getFloat(7);
                float volt_mean = result.getFloat(8);

                SegmentHistory currentSegmentHistory = new SegmentHistory(segmentID, segmentNumber, date, temp_max,
                        temp_min, temp_mean, volt_max, volt_min, volt_mean);
                returnList.add(currentSegmentHistory);
            } while (result.moveToNext());
        }

        result.close();
        db.close();

        return returnList;
    }

    public void deleteAllSegmentEntries() { //use this to delete historical data
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + segment_history_table);
            Log.d("DELETE_ENTRIES", "All entries have been deleted from table " + segment_history_table);
        } catch (Exception e) {
            Log.e("DELETE_ENTRIES_ERROR", "Error while deleting entries from table " + segment_history_table, e);
        } finally {
            db.close();
        }
    }

    public int getEntryCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM your_table";

        return 0;
    }
}
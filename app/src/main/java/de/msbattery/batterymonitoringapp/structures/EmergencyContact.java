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

import androidx.annotation.Nullable;

import java.util.Objects;

public class EmergencyContact {
    private int id; //default value that makes autoincrement assign number
    private String name; //Wherever this will be entered, we need to ensure a char limit
    private String phoneNumber;
    private String email; //Maybe check if this is correct format when they enter it?
    private boolean is_displayed; //i.e. is this one of the two people that will be displayed? maybe change to array?

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIs_displayed() {
        return is_displayed;
    }

    public void setIs_displayed(boolean is_displayed) {
        this.is_displayed = is_displayed;
    }

    public EmergencyContact(String name, String phoneNumber, String email, boolean is_displayed) { //to be used to instantiate a contact
        this.id = -1;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.is_displayed = is_displayed;
    }

    public EmergencyContact(int id, String name, String phoneNumber, String email, boolean is_displayed) { //used by the db internally
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.is_displayed = is_displayed;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        EmergencyContact contact = (EmergencyContact) obj;
        return /*contact.id == this.id*/ contact.is_displayed == this.is_displayed && contact.name.equals(this.name) && contact.email.equals(this.email) && contact.phoneNumber.equals(this.phoneNumber);

    }

    // Overriding hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, phoneNumber);
    }
}
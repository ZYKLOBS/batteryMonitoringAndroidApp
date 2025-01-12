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

public enum ErrorMessage {
    UNDERVOLTAGE("undervoltage detected"),
    OVERVOLTAGE("overvoltage detected"),
    UNDERTEMPERATURE("undertemperature detected"),
    OVERTEMPERATURE("overtemperature detected"),
    OPENLOAD("openload detected"),
    OPENLOAD_BALANCER("openload on cell balancer detected"),
    SHORTCIRCUIT("shortcircuit detected"),
    SHORTCIRCUIT_BALANCER("shortcircuit on cell balancer detected"),
    IC_MALFUNCTION("IC malfunction detected"),
    IC_SUPPLY_LOW("IC supply voltage is low"),
    IC_SUPPLY_HIGH("IC supply voltage too high"),
    IC_OVERHEATING("IC is overheating"),

    VOLTAGE_ERROR("VError"),
    TEMPERATURE_ERROR("TError"),
    TABLE_ERROR("TBError"),
    STATUS_ERROR("failed to determine status"),

    TEMP_UNPLAUSIBLE("metered temperature value is unplausible"),
    VOLT_UNPLAUSIBLE("metered voltage value is unplausible"),

    CHARGING_TOO_FAST("high charging rate detected"),
    DISCHARGING_TOO_FAST("high discharging rate detected"),
    HEATING_TOO_FAST("cell is heating up quickly"),
    COOLING_TOO_FAST("cell is cooling down quickly");



    private String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

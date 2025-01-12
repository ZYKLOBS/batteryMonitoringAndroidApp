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

package de.msbattery.batterymonitoringapp.connection;


import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;


import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.msbattery.batterymonitoringapp.database.DataBaseHelper;
import de.msbattery.batterymonitoringapp.structures.EmergencyContact;

public class AlertSender {

    private String host, port, email, password, subject;
    private ArrayList<EmergencyContact> emergencyContacts;
    
    private int simSlot = 0;

    private boolean phoneNotifiations;
    private boolean mailNotifiations;

    private static AlertSender instance;
    private DataBaseHelper dataBaseHelper;
    private Context lastContext;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    // Pass context when first initializing
    public static synchronized AlertSender getInstance(Context context) {
        if (instance == null) {
            if (context == null)
                return null;
            instance = new AlertSender(context);
        }
        if (context != null) instance.lastContext = context;
        return instance;
    }

    public AlertSender(Context context) {
        this.host = "mail.nikisoft.one";
        this.port = "465";
        this.email = "motorsport@cookiezz.de";
        this.password = "WMcsH8x@";


        lastContext = context;

        dataBaseHelper = new DataBaseHelper(lastContext);

        emergencyContacts = (ArrayList<EmergencyContact>) dataBaseHelper.select_emergency_contacts();
        String phoneNotDB = dataBaseHelper.select_smsNotifications();
        String mailNotDB = dataBaseHelper.select_emailNotifications();
        phoneNotifiations = phoneNotDB.equals("1");
        mailNotifiations = mailNotDB.equals("1");

    }

    public void notifyEmergency(Context context, String message) {
        String subject = "CRITICAL BATTERY STATUS";
        if (emergencyContacts.isEmpty())
            return;
        if (mailNotifiations)
            sendMail(subject, message);
        if (context == null) {
            Log.e("SMS", "Context is null. No SMS was sent");
            return;
        }

        if (phoneNotifiations)
            sendSMS(context, "CRITICAL BATTERY STATUS\n\n" + message, simSlot);
    }

    public void sendMail(String subject, String message) {
        // Use executor to make it run async in background
        ExecutorService executor = Executors.newSingleThreadExecutor();

        String time = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY).format(new Date()) +
                "\n";

        String finalMessage = time + message;
        executor.execute(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");


            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            });
            // Create and send message
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress((email)));

                List<InternetAddress> recipientAddresses = new ArrayList<>();
                int targets = 0;
                for (int i = 0; i < emergencyContacts.size(); i++) {
                    String mail = emergencyContacts.get(i).getEmail();
                    Log.i("AlertSender", "Sending mail to: " + mail);
                    if (mail != null && mail.length() > 4) {
                        recipientAddresses.add(new InternetAddress(mail));
                    }
                }
                if (recipientAddresses.isEmpty()) {
                    Log.e("MailSender", "No valid email addresses found.");
                    return;
                }
                msg.setRecipients(Message.RecipientType.TO,
                        recipientAddresses.toArray(new InternetAddress[0]));

                msg.setSubject(subject);
                msg.setText(finalMessage);
                try {
                    Transport.send(msg);
                    Log.i("MailSender", "Email sent successfully.");
                } catch (MessagingException e) {
                    Log.e("MailSender", "Error sending email: " + e.getMessage(), e);
                }
            } catch (MessagingException e) {
                Log.e("MailSender", "Error creating email message: " + e.getMessage(), e);
            }
        });
    }

    // This code sucks, but I couldn't care less
    public void sendSMS(Context context, String message, int simSlot) {
        if (!phoneNotifiations)
            return;
        PackageManager packageManager = context.getPackageManager();

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            SmsManager smsManager;
            if (simSlot == 0) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // Well, sounds like a you problem
                    return;
                }
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                if (subscriptionInfoList != null && subscriptionInfoList.size() > simSlot) {
                    int subscriptionId = subscriptionInfoList.get(simSlot).getSubscriptionId();
                    smsManager = context.getSystemService(SmsManager.class).createForSubscriptionId(subscriptionId);
                } else {
                    // Fallback to default if SIM slot broken or sth
                    smsManager = context.getSystemService(SmsManager.class);
                }
            }

            for (EmergencyContact contact : emergencyContacts) {
                String number = contact.getPhoneNumber();
                if(number == null)
                    continue;
                // Just in case someone does the funny and enters 110
                if(number.length() < 4)
                    continue;
                String SENT = "SMS_SENT";
                String DELIVERED = "SMS_DELIVERED";

                // Create ArrayList<sentPI>, same for deliveredPI to work with sendMultipartTextMessage
                // You can do this, I believe in you!
                //PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);
                //PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE);

                // Register for sending SMS
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                Log.d("SMS", "SMS sent successfully");
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Log.d("SMS", "Generic failure");
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Log.d("SMS", "No service");
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Log.d("SMS", "Null PDU");
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                Log.d("SMS", "Radio off");
                                break;
                        }
                    }
                }, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);

                // Register for delivery of SMS
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                Log.d("SMS", "SMS delivered");
                                break;
                            case Activity.RESULT_CANCELED:
                                Log.d("SMS", "SMS not delivered");
                                break;
                        }
                    }
                }, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(number, null, parts, null, null);/*sentPI, deliveredPI);*/
            }
        }
    }

    // SMS permission stuff
    public boolean canSendSms(Context context) {
        if (context == null) {
            Log.e("SMS", "Context is null. No SMS was sent");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public boolean isDualSIM(Context context, Activity activity) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
            return false;
        }
        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        return subscriptionInfoList != null && subscriptionInfoList.size() > 1;
    }


    public static boolean addEmergencyContact(Context context, EmergencyContact contact) {
        AlertSender sender = getInstance(context);
        if(sender == null) return false;
        if(sender.lastContext != context) {
            sender.lastContext = context;
            sender.dataBaseHelper = new DataBaseHelper(sender.lastContext);
        }

        if(sender.emergencyContacts.contains(contact)) {
            Log.d("BEWARE!!", "Contact already exists");
            return false;
        }

        if(!sender.dataBaseHelper.insert_emergency_contact(contact)) {
            Log.e("DB", "Unable to add number to database!");
            return false;
        }
        sender.emergencyContacts.add(contact);
        return true;
    }

    public static int setIs_Displayed(Context context, EmergencyContact contact, boolean is_Displayed) {
        AlertSender sender = getInstance(context);
        if(sender == null) return -1;

        if(!sender.emergencyContacts.contains(contact)) {
            Log.e("AlertSender", "Contact does not exists!");
            return 1;
        }
        int status = (is_Displayed ? 1 : 0);
        int maxShown = 3;

        if(is_Displayed) {
            for(int i = 0; i < sender.emergencyContacts.size(); i++) {
                if(sender.emergencyContacts.get(i).getIs_displayed()) {
                    maxShown--;
                }
                if(maxShown == 0) {
                    return 3;
                }
            }
        }

        for(int i = 0; i < sender.emergencyContacts.size(); i++) {
            if(sender.emergencyContacts.get(i).equals(contact)) {
                sender.emergencyContacts.get(i).setIs_displayed(is_Displayed);
            }
        }
        if(!sender.dataBaseHelper.updateEmergencyContactDisplayStatusByName(contact.getName(), status)) {
            Log.e("DB", "Unable to add number to database!");
            return 2;
        }
        return 0;
    }

    public static ArrayList<EmergencyContact> removeEmergencyContact(Context context, String name) {
        AlertSender sender = getInstance(context);
        if(sender == null) return null;

        boolean contains = false;
        int index = 0;
        for(EmergencyContact contact : sender.emergencyContacts) {
            if (contact.getName().equals(name)) {
                contains = true;
                break;
            }
            index++;
        }

        if(!contains) {
            Log.e("AlertSender", "Address not found in list!");
            return null;
        }

        if(sender.lastContext != context) {
            sender.lastContext = context;
            sender.dataBaseHelper = new DataBaseHelper(sender.lastContext);
        }
        sender.dataBaseHelper.deleteEmergencyContactByName(name);
        sender.emergencyContacts.remove(index);
        return sender.emergencyContacts;
    }

    public static ArrayList<EmergencyContact> getEmergencyContacts(Context context) {
        AlertSender sender = getInstance(context);
        if(sender == null) return null;
        return sender.emergencyContacts;
    }


    public static boolean isPhoneNotifications(Context context) {
        AlertSender sender = getInstance(context);
        if(sender == null) return false;
        return sender.phoneNotifiations;
    }

    public static boolean isMailNotifications(Context context) {
        AlertSender sender = getInstance(context);
        if(sender == null) return false;
        return sender.mailNotifiations;
    }

    public static void setMailNotifications(Context context, boolean mailNotifiations) {
        AlertSender sender = getInstance(context);
        if(sender == null) return;
        if(mailNotifiations) {
            if(!sender.dataBaseHelper.turn_emailNotifications_on())
                Log.e("Database", "Error turning on mail notifications, changes are local only");
        } else {
            if(!sender.dataBaseHelper.turn_emailNotifications_off())
                Log.e("Database", "Error turning off mail notifications, changes are local only");
        }

        sender.mailNotifiations = mailNotifiations;
    }

    public static void setPhoneNotifications(Context context, boolean phoneNotifiations) {
        AlertSender sender = getInstance(context);
        if(sender == null) return;
        if(phoneNotifiations)
            sender.dataBaseHelper.turn_smsNotifications_on();
        else
            sender.dataBaseHelper.turn_smsNotifications_off();
        sender.phoneNotifiations = phoneNotifiations;
    }
}

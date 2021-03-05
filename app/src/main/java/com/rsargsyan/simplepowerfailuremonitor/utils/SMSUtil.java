package com.rsargsyan.simplepowerfailuremonitor.utils;

import android.telephony.SmsManager;

import androidx.annotation.NonNull;

public class SMSUtil {

    public static void sendSMS(@NonNull String phoneNo, @NonNull String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg,
                    null, null);
            // TODO: write send status to DB and show the user
        } catch (Exception ex) {
            // TODO: same here
        }
    }
}

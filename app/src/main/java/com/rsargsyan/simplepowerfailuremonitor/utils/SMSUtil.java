package com.rsargsyan.simplepowerfailuremonitor.utils;

import android.content.Context;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import com.rsargsyan.simplepowerfailuremonitor.R;

public class SMSUtil {

    public static void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg,
                    null, null);
            // TODO: write send status to DB and show the user
        } catch (Exception ex) {
            // TODO: same here
        }
    }

    public static String getSMSMsg(@NonNull Context context, boolean isPlugged,
                                   String powerOffMsg, String powerOnMsg) {
        if (powerOffMsg == null) {
            powerOffMsg = context.getResources().getString(R.string.power_is_off);
        }
        if (powerOnMsg == null) {
            powerOnMsg = context.getResources().getString(R.string.power_is_on);
        }
        String result = (isPlugged ? powerOnMsg : powerOffMsg);
        return result;
    }
}

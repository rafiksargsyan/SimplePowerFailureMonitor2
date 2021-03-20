package com.rsargsyan.simplepowerfailuremonitor.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

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

    public static String getSMSMsg(boolean isPlugged, String powerOffMsg, String powerOnMsg) {
        return (isPlugged ? powerOnMsg : powerOffMsg);
    }

    public static SmsMessage[]  getSmsMessagesFromIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return new SmsMessage[0];
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; ++i) {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            }
        }
        return msgs;
    }

    public static boolean smsContainsText(@NonNull Intent intent,
                                          @NonNull String text) {
        for (SmsMessage sms : getSmsMessagesFromIntent(intent)) {
            final String msg = sms.getMessageBody();
            if (msg != null && msg.contains(text)) {
                return true;
            }
        }
        return false;
    }

    public static String getContainingSms(@NonNull Intent intent,
                                           @NonNull String text) {
        for (SmsMessage sms : getSmsMessagesFromIntent(intent)) {
            final String msg = sms.getMessageBody();
            if (msg != null && msg.contains(text)) {
                return msg;
            }
        }
        return null;
    }
}

package com.rsargsyan.simplepowerfailuremonitor;

import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SMSSender implements PowerFailureObserver {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Boolean previousState;
    private final String phoneNumber;

    public SMSSender(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void observe(boolean powerIsOn) {
        if (previousState == null || previousState != powerIsOn) {
            previousState = powerIsOn;
            executor.submit(() -> {
                final String msg = (powerIsOn ? "Power is on" : "Power is off");
                sendSMS(phoneNumber, msg);
            });
        }
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }

    public void sendSMS(@NonNull String phoneNo, @NonNull String msg) {
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

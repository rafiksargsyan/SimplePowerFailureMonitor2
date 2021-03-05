package com.rsargsyan.simplepowerfailuremonitor;

import androidx.annotation.NonNull;

import com.rsargsyan.simplepowerfailuremonitor.utils.SMSUtil;

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
                SMSUtil.sendSMS(phoneNumber, msg);
            });
        }
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }

}

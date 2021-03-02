package com.rsargsyan.simplepowerfailuremonitor;

import android.app.Application;
import android.app.NotificationChannel;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            publishNotificationChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void publishNotificationChannels() {
        NotificationChannel mainNotificationChannel =
                new NotificationChannelBuilder(Constants.MAIN_NOTIFICATION_CHANNEL_ID,
                        "General")
                        .description("General notifications")
                        .showBadge(false)
                        .build();
        NotificationUtil.publishNotificationChannel(this, mainNotificationChannel);
    }
}

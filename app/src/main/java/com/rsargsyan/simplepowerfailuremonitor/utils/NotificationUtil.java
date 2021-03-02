package com.rsargsyan.simplepowerfailuremonitor;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

public class NotificationUtil {
    @TargetApi(Build.VERSION_CODES.O)
    public static void publishNotificationChannel(@NonNull Context context,
                                                        @NonNull NotificationChannel channel) {
        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}

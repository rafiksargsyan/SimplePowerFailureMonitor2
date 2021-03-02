package com.rsargsyan.simplepowerfailuremonitor.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationChannelBuilder {
    private final String channelId;
    private final String name;
    private int importance = NotificationManager.IMPORTANCE_DEFAULT;
    private String description;
    private boolean showBadge;

    public NotificationChannelBuilder(@NonNull String channelId, @NonNull String name) {
        this.channelId = channelId;
        this.name = name;
    }

    public NotificationChannelBuilder description(String description) {
        this.description = description;
        return this;
    }

    @SuppressWarnings("unused")
    public NotificationChannelBuilder importance(int importance) {
        this.importance = importance;
        return this;
    }

    public NotificationChannelBuilder showBadge(boolean showBadge) {
        this.showBadge = showBadge;
        return this;
    }

    public NotificationChannel build() {
        NotificationChannel notificationChannel =
                new NotificationChannel(channelId, name, importance);
        notificationChannel.setDescription(description);
        notificationChannel.setShowBadge(showBadge);
        return notificationChannel;
    }
}

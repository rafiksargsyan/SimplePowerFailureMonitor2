package com.rsargsyan.simplepowerfailuremonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class PowerFailureMonitoringService extends Service {
    private static final int NOTIFICATION_ID = 1; // magic number
    private static final int DUMMY_REQUEST_CODE = 0;

    private BroadcastReceiver receiver;
    private MediaPlayer mp;
    private boolean phoneIsPlugged = false;

    @Override
    public void onCreate() {
        registerChargingStateReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bringToForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        destroyMediaPlayer();
    }

    private void initMediaPlayer() {
        try {
            initMediaPlayer1();
        } catch (IOException e) {
            initMediaPlayer2();
        }
    }

    private void initMediaPlayer2() {
        mp = MediaPlayer.create(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
        mp.setLooping(true);
    }

    private void initMediaPlayer1() throws IOException {
        mp = new MediaPlayer();
        mp.setLooping(true);
        mp.setDataSource(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mp.setAudioStreamType(AudioManager.STREAM_ALARM);
        mp.prepare();
    }

    private void destroyMediaPlayer() {
         if (mp != null) {
             mp.release();
         }
    }

    private void registerChargingStateReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        receiver = new ChargingStateReceiver();
        registerReceiver(receiver, filter);
    }

    private Notification createNotification(boolean phoneIsPlugged) {
        @DrawableRes final int largeIconInt =
                (phoneIsPlugged ? R.drawable.ic_battery_charging_sharp_green_24dp
                        : R.drawable.ic_battery_alert_sharp_red_24dp);
        final Drawable drawable = ContextCompat.getDrawable(this, largeIconInt);
        final Bitmap bitmap = DrawableUtil.drawableToBitmap(drawable);
        final String contentTitle =
                (phoneIsPlugged ? "The phone is plugged" : "The phone is not plugged");
        return createNotification(contentTitle, bitmap, createMainActivityIntent());
    }

    private void bringToForeground() {
        Notification notification = createNotification(phoneIsPlugged);
        startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent createMainActivityIntent() {
        return PendingIntent.getActivity(this, DUMMY_REQUEST_CODE,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification createNotification(String contentTitle, Bitmap largeIcon,
                                            PendingIntent intent) {
        return new NotificationCompat.Builder(this,
                Constants.MAIN_NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle(contentTitle)
                .setContentText("Click to manage")
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_bolt_black_24dp)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void handle(int pluggedValue) {
        boolean previousState = phoneIsPlugged;
        phoneIsPlugged = pluggedValue != 0;
        if (previousState == phoneIsPlugged) {
            return;
        }

        Notification notification = createNotification(phoneIsPlugged);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);

        if (!phoneIsPlugged) {
            destroyMediaPlayer();
            initMediaPlayer();
            mp.start();
        } else {
            if (mp != null) {
                mp.stop();
            }
        }
    }

    private class ChargingStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pluggedValue = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            handle(pluggedValue);
        }
    }
}

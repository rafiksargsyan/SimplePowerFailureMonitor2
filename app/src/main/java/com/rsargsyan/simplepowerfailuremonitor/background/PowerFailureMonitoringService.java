package com.rsargsyan.simplepowerfailuremonitor.background;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.rsargsyan.simplepowerfailuremonitor.HumanInteractionDetector;
import com.rsargsyan.simplepowerfailuremonitor.PowerFailureObserver;
import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.SMSSender;
import com.rsargsyan.simplepowerfailuremonitor.ui.MainActivity;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;
import com.rsargsyan.simplepowerfailuremonitor.utils.DrawableUtil;

import java.io.IOException;

public class PowerFailureMonitoringService extends Service{
    private static final int NOTIFICATION_ID = 1; // magic number
    private static final int DUMMY_REQUEST_CODE = 0;
    private static final String SMART_CANCEL_KEY = "smart_cancel";
    private static final String SEND_SMS_KEY = "send_sms";
    private static final String PHONE_NUMBER_KEY = "phone_number";

    private BroadcastReceiver receiver;
    private MediaPlayer mp;
    private boolean phoneIsPlugged = false;
    private boolean alarmIsOn = false;
    private boolean smartCancel;

    private PowerFailureObserver smsSender;
    private boolean sendSMS;
    private String phoneNumber;

    private HumanInteractionDetector humanInteractionDetector;

    @Override
    public void onCreate() {
        registerChargingStateReceiver();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        smartCancel = sharedPreferences.getBoolean(SMART_CANCEL_KEY, false);

        sendSMS = sharedPreferences.getBoolean(SEND_SMS_KEY, false);
        phoneNumber = sharedPreferences.getString(PHONE_NUMBER_KEY, null);
        if (sendSMS && phoneNumber != null) {
            smsSender = new SMSSender(phoneNumber);
        }

        humanInteractionDetector = new HumanInteractionDetector(this, () -> {
            if(smartCancel && alarmIsOn) {
                stopForeground(true);
                stopSelf();
            }
        });
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
        if (smsSender != null) {
            smsSender.destroy();
        }
        humanInteractionDetector.unregister();
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
            alarmIsOn = true;
        } else {
            if (mp != null) {
                mp.stop();
            }
            alarmIsOn = false;
        }

        if (smsSender != null) {
            smsSender.observe(phoneIsPlugged);
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

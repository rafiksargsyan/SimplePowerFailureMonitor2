package com.rsargsyan.simplepowerfailuremonitor.background;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.ui.SmsAlarmCancelActivity;
import com.rsargsyan.simplepowerfailuremonitor.utils.AlarmPlayer;
import com.rsargsyan.simplepowerfailuremonitor.viewmodel.SharedPreferenceLiveData;

import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.ACTION_CANCEL_SMS_ALARM;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.ALARM_SOUND_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_ALARM_NOTIFICATION_CHANNEL_ID;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_ALARM_NOTIFICATION_ID;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_MSG_EXTRA_KEY;

public class SMSReceivedAlarmService extends LifecycleService {
    private AlarmPlayer alarmPlayer;
    private boolean firstTime = true;

    private LiveData<String> alarmSound;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        alarmSound = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, ALARM_SOUND_KEY);
        alarmSound.observeForever(s -> { });
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CANCEL_SMS_ALARM.equals(intent.getAction())) {
            stopFullScreenActivity();
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        Intent cancelIntent = new Intent(this, SMSReceivedAlarmService.class);
        cancelIntent.setAction(ACTION_CANCEL_SMS_ALARM);
        PendingIntent cancelPendingIntent =
                PendingIntent.getService(this, 0, cancelIntent, 0);

        Intent fullScreenIntent = new Intent(this, SmsAlarmCancelActivity.class);
        PendingIntent fullScreenPendingIntent =
                PendingIntent.getActivity(this, 0, fullScreenIntent, 0);

        final String smsMsg = (intent != null ? intent.getStringExtra(SMS_MSG_EXTRA_KEY) : "");
        Notification smsAlarmNotification =
                new NotificationCompat.Builder(this, SMS_ALARM_NOTIFICATION_CHANNEL_ID)
                        .setOngoing(true)
                        .setContentTitle(getString(R.string.alarming_sms_received_notif))
                        .setContentText(smsMsg)
                        .setSilent(true)
                        .setSmallIcon(R.drawable.ic_bolt_black_24dp)
                        .addAction(R.drawable.ic_sharp_close_24,
                                getString(android.R.string.cancel),
                                cancelPendingIntent)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setContentIntent(fullScreenPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .build();
        if (firstTime) {
            startForeground(SMS_ALARM_NOTIFICATION_ID, smsAlarmNotification);
            firstTime = false;
        } else {
            NotificationManagerCompat.from(this)
                    .notify(SMS_ALARM_NOTIFICATION_ID, smsAlarmNotification);
        }

        if (alarmPlayer != null) alarmPlayer.stop();
        alarmPlayer = new AlarmPlayer(this, Uri.parse(alarmSound.getValue()));
        alarmPlayer.play();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (alarmPlayer != null) {
            alarmPlayer.stop();
        }
        super.onDestroy();
    }

    private void stopFullScreenActivity() {
        Intent stopIntent = new Intent(this, SmsAlarmCancelActivity.class);
        stopIntent.setAction(ACTION_CANCEL_SMS_ALARM);
        startActivity(stopIntent);
    }
}

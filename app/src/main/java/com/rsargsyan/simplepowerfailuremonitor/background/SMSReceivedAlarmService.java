package com.rsargsyan.simplepowerfailuremonitor.background;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.ui.SmsAlarmCancelActivity;
import com.rsargsyan.simplepowerfailuremonitor.utils.AlarmPlayer;

import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.ACTION_CANCEL_SMS_ALARM;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_ALARM_NOTIFICATION_CHANNEL_ID;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMS_ALARM_NOTIFICATION_ID;

public class SMSReceivedAlarmService extends LifecycleService {
    private AlarmPlayer alarmPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent cancelIntent = new Intent(this, SMSReceivedAlarmService.class);
        cancelIntent.setAction(ACTION_CANCEL_SMS_ALARM);
        PendingIntent cancelPendingIntent =
                PendingIntent.getService(this, 0, cancelIntent, 0);

        Intent fullScreenIntent = new Intent(this, SmsAlarmCancelActivity.class);
        PendingIntent fullScreenPendingIntent =
                PendingIntent.getActivity(this, 0, fullScreenIntent, 0);

        Notification smsAlarmNotification =
                new NotificationCompat.Builder(this, SMS_ALARM_NOTIFICATION_CHANNEL_ID)
                        .setOngoing(true)
                        .setContentTitle("TestTitle")
                        .setContentText("TestText")
                        .setSilent(true)
                        .setSmallIcon(R.drawable.ic_bolt_black_24dp)
                        .addAction(R.drawable.ic_bolt_black_24dp, "CANCEL",
                                cancelPendingIntent)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setContentIntent(fullScreenPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .build();

        startForeground(SMS_ALARM_NOTIFICATION_ID, smsAlarmNotification);

        alarmPlayer = new AlarmPlayer(this);
        alarmPlayer.play();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CANCEL_SMS_ALARM.equals(intent.getAction())) {
            stopFullScreenActivity();
            stopForeground(true);
            stopSelf();
        }

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

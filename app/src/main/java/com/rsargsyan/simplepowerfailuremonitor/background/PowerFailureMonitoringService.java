package com.rsargsyan.simplepowerfailuremonitor.background;

import android.app.Notification;
import android.app.PendingIntent;
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
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.rsargsyan.simplepowerfailuremonitor.HumanInteractionDetector;
import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.SharedPreferenceLiveData;
import com.rsargsyan.simplepowerfailuremonitor.ui.MainActivity;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;
import com.rsargsyan.simplepowerfailuremonitor.utils.DrawableUtil;
import com.rsargsyan.simplepowerfailuremonitor.utils.SMSUtil;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PowerFailureMonitoringService extends LifecycleService {
    private static final int NOTIFICATION_ID = 1; // magic number
    private static final int DUMMY_REQUEST_CODE = 0;
    private static final String SMART_CANCEL_KEY = "smart_cancel";
    private static final String SEND_SMS_KEY = "send_sms";
    private static final String PHONE_NUMBER_KEY = "phone_number";

    private final ExecutorService smsSenderExecutor = Executors.newSingleThreadExecutor();

    private SharedPreferences sharedPreferences;

    private LiveData<Boolean> smartCancelLive;
    private LiveData<Boolean> sendSMSLive;
    private LiveData<String> phoneNumberLive;

    private BroadcastReceiver receiver;
    private MediaPlayer mp;
    private Boolean phoneIsPlugged;
    private HumanInteractionDetector humanInteractionDetector;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        smartCancelLive =
                new SharedPreferenceLiveData<>(Boolean.class, sharedPreferences, SMART_CANCEL_KEY);

        sendSMSLive =
                new SharedPreferenceLiveData<>(Boolean.class, sharedPreferences, SEND_SMS_KEY);

        phoneNumberLive =
                new SharedPreferenceLiveData<>(String.class, sharedPreferences, PHONE_NUMBER_KEY);

        humanInteractionDetector =
                new HumanInteractionDetector(this, () -> {
                    if (mp != null) {
                        mp.stop();
                    }
                });

        registerChargingStateReceiver();

        smartCancelLive.observe(this, smartCancelEnabled -> {
            if (smartCancelEnabled == null || !smartCancelEnabled) {
                humanInteractionDetector.unregister();
            } else {
                humanInteractionDetector.register();
            }
        });

        sendSMSLive.observe(this, sendSMS -> { /*NOOP*/ });
        phoneNumberLive.observe(this, phoneNumber -> { /*NOOP*/ });
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        bringToForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        destroyMediaPlayer();
        humanInteractionDetector.unregister();
        smsSenderExecutor.shutdown();
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

    private Notification createNotification(Boolean phoneIsPlugged) {
        @DrawableRes int largeIconInt;
        Drawable drawable;
        Bitmap bitmap =  null;
        String contentTitle = null;
        if (phoneIsPlugged != null) {
            largeIconInt =
                    (phoneIsPlugged ? R.drawable.ic_battery_charging_sharp_green_24dp
                            : R.drawable.ic_battery_alert_sharp_red_24dp);
            drawable = ContextCompat.getDrawable(this, largeIconInt);
            bitmap = DrawableUtil.drawableToBitmap(drawable);
            contentTitle =
                    (phoneIsPlugged ? "The phone is plugged" : "The phone is not plugged");
        }
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

    private void handle(boolean isPlugged) {
        if (phoneIsPlugged != null && phoneIsPlugged == isPlugged) {
            return;
        }

        Notification notification = createNotification(isPlugged);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);

        if (isPlugged) {
            if (mp != null) {
                mp.stop();
            }
        } else if (phoneIsPlugged != null) {
            destroyMediaPlayer();
            initMediaPlayer();
            mp.start();
        }

        if (shouldSendSMS(isPlugged)) {
            smsSenderExecutor.submit(() -> {
                final String phoneNumber = phoneNumberLive.getValue();
                if (phoneNumber != null) {
                    final String msg = (isPlugged ? "Power is on" : "Power is off");
                    SMSUtil.sendSMS(phoneNumber, msg);
                }
            });
        }

        phoneIsPlugged = isPlugged;
    }

    private boolean shouldSendSMS(boolean isPlugged) {
        final Boolean sendSMS = sendSMSLive.getValue();
        return sendSMS != null && sendSMS
                && (phoneIsPlugged == null || phoneIsPlugged != isPlugged);
    }

    private class ChargingStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int pluggedValue =
                    intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            handle(pluggedValue != 0);
        }
    }
}

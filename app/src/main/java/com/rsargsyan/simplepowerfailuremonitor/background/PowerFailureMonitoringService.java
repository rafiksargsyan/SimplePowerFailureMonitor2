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
import android.os.BatteryManager;
import android.os.IBinder;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.rsargsyan.simplepowerfailuremonitor.utils.AlarmPlayer;
import com.rsargsyan.simplepowerfailuremonitor.utils.HumanInteractionDetector;
import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.viewmodel.SharedPreferenceLiveData;
import com.rsargsyan.simplepowerfailuremonitor.ui.MainActivity;
import com.rsargsyan.simplepowerfailuremonitor.utils.DrawableUtil;
import com.rsargsyan.simplepowerfailuremonitor.utils.SMSUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.nedim.maildroidx.MaildroidX;
import co.nedim.maildroidx.MaildroidXType;

import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.ALARM_SOUND_SMART_MUTE_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.DEFAULT_EMAIL_SUBJECT;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.DEFAULT_SMTP_PASSWORD;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.DEFAULT_SMTP_PORT;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.DEFAULT_SMTP_SERVER;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.DEFAULT_SMTP_USERNAME;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.EMAIL_USE_DEFAULT_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.MAIN_NOTIFICATION_CHANNEL_ID;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.MONITORING_NOTIFICATION_ID;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.PHONE_NUMBER_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.PLAY_ALARM_SOUND_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.POWER_OFF_BODY_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.POWER_OFF_MSG_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.POWER_ON_BODY_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.POWER_ON_MSG_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.RECIPIENT_EMAIL_ADDRESS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SENDER_EMAIL_ADDRESS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SENDER_EMAIL_PASSWORD_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SEND_EMAIL_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SEND_SMS_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMTP_PORT_KEY;
import static com.rsargsyan.simplepowerfailuremonitor.utils.Constants.SMTP_SERVER_KEY;

public class PowerFailureMonitoringService extends LifecycleService {
    private static final int DUMMY_REQUEST_CODE = 0;

    private final ExecutorService smsSenderExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService emailSenderExecutor = Executors.newSingleThreadExecutor();

    private LiveData<Boolean> playAlarm;
    private LiveData<Boolean> sendSMS;
    private LiveData<String> phoneNumber;
    private LiveData<String> powerOffMsg;
    private LiveData<String> powerOnMsg;

    private LiveData<Boolean> sendEmail;
    private LiveData<String> recipientEmailAddress;
    private LiveData<Boolean> useDefaultEmail;
    private LiveData<String> smtpServer;
    private LiveData<String> smtpPort;
    private LiveData<String> senderEmailAddress;
    private LiveData<String> senderPassword;
    private LiveData<String> powerOffBody;
    private LiveData<String> powerOnBody;

    private BroadcastReceiver receiver;
    private AlarmPlayer alarmPlayer;
    private Boolean phoneIsPlugged;
    private HumanInteractionDetector humanInteractionDetector;

    @Override
    public void onCreate() {
        super.onCreate();

        registerChargingStateReceiver();

        setupLiveData();

        initHumanInteractionDetector();
    }

    private void initHumanInteractionDetector() {
        humanInteractionDetector =
                new HumanInteractionDetector(this, () -> {
                    if (alarmPlayer != null) {
                        alarmPlayer.stop();
                    }
                });
    }

    private void setupLiveData() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        playAlarm = new SharedPreferenceLiveData<>(Boolean.class,
                sharedPreferences, PLAY_ALARM_SOUND_KEY);
        playAlarm.observe(this, it -> {/*NOOP*/});

        LiveData<Boolean> smartMute = new SharedPreferenceLiveData<>(Boolean.class,
                sharedPreferences, ALARM_SOUND_SMART_MUTE_KEY);
        smartMute.observe(this, smartMuteEnabled -> {
            if (smartMuteEnabled == null || !smartMuteEnabled) {
                humanInteractionDetector.unregister();
            } else {
                humanInteractionDetector.register();
            }
        });

        sendSMS = new SharedPreferenceLiveData<>(Boolean.class, sharedPreferences, SEND_SMS_KEY);
        sendSMS.observe(this, it -> { /*NOOP*/ });

        phoneNumber =
                new SharedPreferenceLiveData<>(String.class, sharedPreferences, PHONE_NUMBER_KEY);
        phoneNumber.observe(this, it -> { /*NOOP*/ });

        powerOffMsg =
                new SharedPreferenceLiveData<>(String.class, sharedPreferences, POWER_OFF_MSG_KEY);
        powerOffMsg.observe(this, it -> { /*NOOP*/ });

        powerOnMsg =
                new SharedPreferenceLiveData<>(String.class, sharedPreferences, POWER_ON_MSG_KEY);
        powerOnMsg.observe(this, it -> { /*NOOP*/ });

        sendEmail =
                new SharedPreferenceLiveData<>(Boolean.class, sharedPreferences, SEND_EMAIL_KEY);
        sendEmail.observe(this, it -> { /*NOOP*/ });

        recipientEmailAddress = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, RECIPIENT_EMAIL_ADDRESS_KEY);
        recipientEmailAddress.observe(this, it -> { /*NOOP*/ });

        useDefaultEmail = new SharedPreferenceLiveData<>(Boolean.class,
                sharedPreferences, EMAIL_USE_DEFAULT_KEY);
        useDefaultEmail.observe(this, it -> { /*NOOP*/ });

        smtpServer = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, SMTP_SERVER_KEY);
        smtpServer.observe(this, it -> { /*NOOP*/ });

        smtpPort = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, SMTP_PORT_KEY);
        smtpPort.observe(this, it -> { /*NOOP*/ });

        senderEmailAddress = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, SENDER_EMAIL_ADDRESS_KEY);
        senderEmailAddress.observe(this, it -> { /*NOOP*/ });

        senderPassword =
                new SharedPreferenceLiveData<>(String.class, sharedPreferences,
                        SENDER_EMAIL_PASSWORD_KEY);
        senderPassword.observe(this, it -> { /*NOOP*/ });

        powerOffBody = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, POWER_OFF_BODY_KEY);
        powerOffBody.observe(this, it -> { /*NOOP*/ });

        powerOnBody = new SharedPreferenceLiveData<>(String.class,
                sharedPreferences, POWER_ON_BODY_KEY);
        powerOnBody.observe(this, it -> { /*NOOP*/ });
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        bringToForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        destroyAlarmPlayer();
        humanInteractionDetector.unregister();
        smsSenderExecutor.shutdown();
        emailSenderExecutor.shutdown();
    }

    private void destroyAlarmPlayer() {
        if (alarmPlayer != null) {
            alarmPlayer.stop();
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
        startForeground(MONITORING_NOTIFICATION_ID, notification);
    }

    private PendingIntent createMainActivityIntent() {
        return PendingIntent.getActivity(this, DUMMY_REQUEST_CODE,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification createNotification(String contentTitle, Bitmap largeIcon,
                                            PendingIntent intent) {
        return new NotificationCompat.Builder(this, MAIN_NOTIFICATION_CHANNEL_ID)
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
        NotificationManagerCompat.from(this).notify(MONITORING_NOTIFICATION_ID, notification);

        if (shouldPlayAlarm(isPlugged)) {
            if (alarmPlayer == null) {
                alarmPlayer = new AlarmPlayer(this);
            }
            alarmPlayer.play();
        } else if (alarmPlayer != null){
            alarmPlayer.stop();
        }

        if (shouldSendSMS(isPlugged)) {
            final String phoneNumberValue = phoneNumber.getValue();
            final String msg =
                    SMSUtil.getSMSMsg(this, isPlugged,
                            powerOffMsg.getValue(), powerOnMsg.getValue());
            smsSenderExecutor.submit(() -> SMSUtil.sendSMS(phoneNumberValue, msg));
        }

        if (shouldSendEmail(isPlugged)) {
            final String emailBody =
                    SMSUtil.getSMSMsg(this, isPlugged,
                            powerOffBody.getValue(), powerOnBody.getValue());
            sendEmail(emailBody);
        }

        phoneIsPlugged = isPlugged;
    }

    private void sendEmail(String body) {
        String smtpServer = DEFAULT_SMTP_SERVER;
        String smtpUsername = DEFAULT_SMTP_USERNAME;
        String smtpPassword = DEFAULT_SMTP_PASSWORD;
        String port = DEFAULT_SMTP_PORT;
        final String to = recipientEmailAddress.getValue();
        String from = smtpUsername;

        if (!shouldUseDefaultEmail()) {
            smtpServer = this.smtpServer.getValue();
            smtpUsername = senderEmailAddress.getValue();
            smtpPassword = senderPassword.getValue();
            port = smtpPort.getValue();
            from = smtpUsername;
        }

        final String smtpServerFinal = smtpServer;
        final String smtpUsernameFinal = smtpUsername;
        final String smtpPasswordFinal = smtpPassword;
        final String fromFinal = from;
        final String portFinal = port;

        emailSenderExecutor.submit(() -> new MaildroidX.Builder()
                .smtp(smtpServerFinal)
                .smtpUsername(smtpUsernameFinal)
                .smtpPassword(smtpPasswordFinal)
                .port(portFinal)
                .type(MaildroidXType.PLAIN)
                .to(to)
                .from(fromFinal)
                .subject(DEFAULT_EMAIL_SUBJECT)
                .body(body)
                .mail());
    }

    private boolean shouldUseDefaultEmail() {
        final Boolean useDefault = useDefaultEmail.getValue();
        return useDefault == null || useDefault;
    }

    private boolean shouldSendSMS(boolean isPlugged) {
        final Boolean sendSMSValue = sendSMS.getValue();
        return sendSMSValue != null && sendSMSValue && plugStateChanged(isPlugged);
    }

    private boolean shouldSendEmail(boolean isPlugged) {
        final Boolean sendEmailValue = sendEmail.getValue();
        return sendEmailValue != null && sendEmailValue && plugStateChanged(isPlugged);
    }

    private boolean shouldPlayAlarm(boolean isPlugged) {
        final Boolean playAlarmValue = playAlarm.getValue();
        return (playAlarmValue == null || playAlarmValue)
                && phoneIsPlugged != null && phoneIsPlugged && !isPlugged;
    }

    private boolean plugStateChanged(boolean isPlugged) {
        return phoneIsPlugged == null || phoneIsPlugged != isPlugged;
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

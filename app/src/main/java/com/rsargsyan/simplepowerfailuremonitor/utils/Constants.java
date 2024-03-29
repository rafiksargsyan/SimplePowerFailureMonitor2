package com.rsargsyan.simplepowerfailuremonitor.utils;

public class Constants {
    public static final String MAIN_NOTIFICATION_CHANNEL_ID = "MAIN_NOTIFICATION";
    public static final String SMS_ALARM_NOTIFICATION_CHANNEL_ID = "SMS_ALARM_NOTIFICATION_CHANNEL";
    public static final String MONITORING_PREFERENCES = "monitoring_preferences";
    public static final String MONITORING_STARTED_KEY = "MONITORING_STARTED";
    public static final boolean MONITORING_STARTED_DEFAULT = false;
    public static final int MONITORING_NOTIFICATION_ID = 1;
    public static final int SMS_ALARM_NOTIFICATION_ID = 2;

    // Alarm preferences
    public static final String PLAY_ALARM_SOUND_KEY = "play_alarm_sound";
    public static final String ALARM_SOUND_SMART_MUTE_KEY = "smart_cancel";
    public static final String ALARM_SOUND_KEY = "alarm_sound";
    public static final String ALARM_SETTINGS_KEY = "alarm_settings";

    // SMS preferences
    public static final String SEND_SMS_KEY = "send_sms";
    public static final String PHONE_NUMBER_KEY = "phone_number";
    public static final String POWER_OFF_MSG_KEY = "power_off_msg";
    public static final String POWER_ON_MSG_KEY = "power_on_msg";

    // Email preferences
    public static final String SEND_EMAIL_KEY = "send_email";
    public static final String RECIPIENT_EMAIL_ADDRESS_KEY = "recipient_email_address";
    public static final String POWER_OFF_BODY_KEY = "power_off_body";
    public static final String POWER_ON_BODY_KEY = "power_on_body";
    public static final String EMAIL_USE_DEFAULT_KEY = "email_use_default";
    public static final String SMTP_SERVER_KEY = "smtp_server";
    public static final String SMTP_PORT_KEY = "smtp_port";
    public static final String SENDER_EMAIL_ADDRESS_KEY = "email_address";
    public static final String SENDER_EMAIL_PASSWORD_KEY = "email_password";

    // Default SMTP settings
    public static final String DEFAULT_SMTP_PORT = "465";

    // SMS alarm settings
    public static final String SMS_ALARM_KEY = "sms_alarm";
    public static final String ALARMING_MESSAGE_KEY = "alarming_message";

    public static final String ACTION_CANCEL_SMS_ALARM =
            "com.rsargsyan.simplepowerfailuremonitor.ACTION_CANCEL_SMS_ALARM";
    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public static final String SMS_MSG_EXTRA_KEY = "SMS_MSG_EXTRA";
}

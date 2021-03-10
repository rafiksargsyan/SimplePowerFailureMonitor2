package com.rsargsyan.simplepowerfailuremonitor.utils;

public class Constants {
    public static final String MAIN_NOTIFICATION_CHANNEL_ID = "MAIN_NOTIFICATION";
    public static final String MONITORING_PREFERENCES = "monitoring_preferences";
    public static final String MONITORING_STARTED_KEY = "MONITORING_STARTED";
    public static final boolean MONITORING_STARTED_DEFAULT = false;
    public static final int MONITORING_NOTIFICATION_ID = 1;
    public static final int SMS_ALARM_NOTIFICATION_ID = 2;

    // Alarm preferences
    public static final String PLAY_ALARM_SOUND_KEY = "play_alarm_sound";
    public static final String ALARM_SOUND_SMART_MUTE_KEY = "smart_cancel";

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
    public static final String DEFAULT_SMTP_SERVER = "smtp.gmail.com";
    public static final String DEFAULT_SMTP_USERNAME = "simplepowerfailuremonitor@gmail.com";
    public static final String DEFAULT_SMTP_PASSWORD = "~/&LX)@5w9KS^2#>";
    public static final String DEFAULT_SMTP_PORT = "465";
    public static final String DEFAULT_EMAIL_SUBJECT = "Power state changed";

    public static final String ACTION_CANCEL_SMS_ALARM =
            "com.rsargsyan.simplepowerfailuremonitor.ACTION_CANCEL_SMS_ALARM";

}

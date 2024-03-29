package com.rsargsyan.simplepowerfailuremonitor.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.background.SMSReceivedAlarmService;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;

public class SmsAlarmCancelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && Constants.ACTION_CANCEL_SMS_ALARM.equals(intent.getAction())) {
            finish();
        }

        setContentView(R.layout.activity_sms_alarm_cancel);

        makeActivityVisibleOnLockScreen();

        View smsAlarmCancelFab = findViewById(R.id.cancel_sms_alarm_fab);
        smsAlarmCancelFab.setOnClickListener(v -> {
            stopService(new Intent(this, SMSReceivedAlarmService.class));
            finish();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && Constants.ACTION_CANCEL_SMS_ALARM.equals(intent.getAction())) {
            finish();
        }
    }

    private void makeActivityVisibleOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            KeyguardManager keyguardManager =
                    (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }
}
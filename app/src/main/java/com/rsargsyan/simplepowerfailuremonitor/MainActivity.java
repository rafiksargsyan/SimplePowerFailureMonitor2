package com.rsargsyan.simplepowerfailuremonitor;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.rsargsyan.simplepowerfailuremonitor.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String MONITORING_PREFERENCES = "monitoring_preferences";
    private static final String MONITORING_STARTED_KEY = "MONITORING_STARTED";
    private static final boolean MONITORING_STARTED_DEFAULT = false;

    private ActivityMainBinding binding;
    private boolean monitoringIsStarted;
    private SharedPreferences sharedPreferences;
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        registerStartStopMonitorOnClickListener();

        initSharedPreferences();

        updateMonitoringState();

        registerChargingStateReceiver();

        // In case monitoring service has died for some reason
        if (monitoringIsStarted) {
            startMonitoringService();
        }
    }

    private void registerChargingStateReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        receiver = new ChargingStateReceiver();
        registerReceiver(receiver, filter);
    }

    private void updateMonitoringState() {
        monitoringIsStarted =
                sharedPreferences.getBoolean(MONITORING_STARTED_KEY, MONITORING_STARTED_DEFAULT);
        @DrawableRes int drawable =
                (monitoringIsStarted ? R.drawable.ic_baseline_pause_24
                        : R.drawable.ic_baseline_play_arrow_24);
        binding.startStopMonitorFab.setImageResource(drawable);
    }

    private void initSharedPreferences() {
        sharedPreferences =
                getApplicationContext().getSharedPreferences(MONITORING_PREFERENCES,
                        Context.MODE_PRIVATE);
        // Need to have strong reference to the listener
        prefChangeListener = (sharedPreferences, key) -> {
            if (key != null) {
                updateMonitoringState();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
    }

    private void registerStartStopMonitorOnClickListener() {
        binding.startStopMonitorFab.setOnClickListener(v -> {
            if (monitoringIsStarted) {
                stopService(new Intent(this, PowerFailureMonitoringService.class));
                Toast.makeText(this,
                        "Monitoring has been stopped!", Toast.LENGTH_SHORT).show();
            } else {
                startMonitoringService();
                Toast.makeText(this,
                        "Monitoring has been started!", Toast.LENGTH_SHORT).show();
            }
            sharedPreferences.edit()
                    .putBoolean(MONITORING_STARTED_KEY, !monitoringIsStarted).apply();
        });
    }

    private void startMonitoringService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this,
                    PowerFailureMonitoringService.class));
        } else {
            startService(new Intent(this,
                    PowerFailureMonitoringService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private class ChargingStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pluggedValue = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            if (0 == pluggedValue) {
                binding.chargingStateImageView.setImageDrawable(
                        ContextCompat.getDrawable(MainActivity.this,
                                R.drawable.ic_battery_alert_sharp_red_256dp));
            } else {
                binding.chargingStateImageView.setImageDrawable(
                        ContextCompat.getDrawable(MainActivity.this,
                                R.drawable.ic_battery_charging_sharp_green_256dp));
            }
        }
    }
}
package com.rsargsyan.simplepowerfailuremonitor.ui;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rsargsyan.simplepowerfailuremonitor.MainAndroidViewModelFactory;
import com.rsargsyan.simplepowerfailuremonitor.MainViewModel;
import com.rsargsyan.simplepowerfailuremonitor.background.PowerFailureMonitoringService;
import com.rsargsyan.simplepowerfailuremonitor.R;
import com.rsargsyan.simplepowerfailuremonitor.databinding.ActivityMainBinding;
import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BroadcastReceiver receiver;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 0);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel =
                new ViewModelProvider(this, new MainAndroidViewModelFactory(getApplication()))
                        .get(MainViewModel.class);

        mainViewModel.getMonitoringIsStarted().observe(this, (Observer<Boolean>) o -> {
            if (o == null) o = Constants.MONITORING_STARTED_DEFAULT;
            @DrawableRes int drawable =
                    (o ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24);
            binding.startStopMonitorFab.setImageResource(drawable);
            if (o) {
                startMonitoringService();
                Toast.makeText(this,
                        "Monitoring has been started!", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, PowerFailureMonitoringService.class));
                Toast.makeText(this,
                        "Monitoring has been stopped!", Toast.LENGTH_SHORT).show();
            }
        });

        registerStartStopMonitorOnClickListener();

        registerChargingStateReceiver();
    }

    private void registerChargingStateReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        receiver = new ChargingStateReceiver();
        registerReceiver(receiver, filter);
    }

    private void registerStartStopMonitorOnClickListener() {
        binding.startStopMonitorFab.setOnClickListener(v -> {
            Boolean monitoringIsStarted = mainViewModel.getMonitoringIsStarted().getValue();
            if (monitoringIsStarted == null) {
                monitoringIsStarted = Constants.MONITORING_STARTED_DEFAULT;
            }
            mainViewModel.setMonitoringIsStarted(!monitoringIsStarted);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings_item) {
            openSettingsActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
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
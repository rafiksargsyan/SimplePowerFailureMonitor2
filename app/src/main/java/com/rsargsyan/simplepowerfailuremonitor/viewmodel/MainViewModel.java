package com.rsargsyan.simplepowerfailuremonitor.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.rsargsyan.simplepowerfailuremonitor.utils.Constants;

public class MainViewModel extends AndroidViewModel {
    private final SharedPreferences defaultSharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication());
    private final SharedPreferences monitoringSharedPreferences =
            getApplication().getSharedPreferences(Constants.MONITORING_PREFERENCES,
                    Context.MODE_PRIVATE);

    private final LiveData<Boolean> monitoringIsStarted =
            new SharedPreferenceLiveData<>(Boolean.class, monitoringSharedPreferences,
                    Constants.MONITORING_STARTED_KEY);
    private final MutableLiveData<Boolean> isPlugged = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getMonitoringIsStarted() {
        return monitoringIsStarted;
    }

    public void setMonitoringIsStarted(boolean isStarted) {
        monitoringSharedPreferences.edit().
                putBoolean(Constants.MONITORING_STARTED_KEY, isStarted).apply();
    }

    public LiveData<Boolean> getIsPlugged() {
        return isPlugged;
    }

    public void setIsPlugged(boolean isPlugged) {
        this.isPlugged.setValue(isPlugged);
    }
}

package com.rsargsyan.simplepowerfailuremonitor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.Map;
import java.util.Set;

public class SharedPreferenceLiveData<T> extends LiveData<T> {
    private final SharedPreferences sharedPreferences;
    private final Class<T> type;
    private final OnSharedPreferenceChangeListener listener;
    private final String key;

    public SharedPreferenceLiveData(@NonNull Class<T> type,
                                    @NonNull SharedPreferences sharedPreferences,
                                    @NonNull String key) {
        this.sharedPreferences = sharedPreferences;
        this.type = type;
        this.key = key;
        listener = (sharedPrefs, k) -> {
            if (!key.equals(k)) return;
            setValue(getPreference(sharedPrefs, k));
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        setValue(getPreference(sharedPreferences, key));
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @SuppressWarnings("unchecked")
    private T getPreference(SharedPreferences sharedPrefs, String k) {
        Map<String, ?> all = sharedPrefs.getAll();
        if (!all.containsKey(k)) return null;
        Object preference = null;
        if (type == Boolean.class) {
            preference = sharedPrefs.getBoolean(k, false);
        } else if (type == Float.class) {
            preference = sharedPrefs.getFloat(k, 0);
        } else if (type == Integer.class) {
            preference = sharedPrefs.getInt(k, 0);
        } else if (type == Long.class) {
            preference = sharedPrefs.getLong(k, 0);
        } else if (type == String.class) {
            preference = sharedPrefs.getString(k, null);
        } else if (type == Set.class ) {
            preference = sharedPrefs.getStringSet(k,null);
        }
        return (T)preference;
    }
}

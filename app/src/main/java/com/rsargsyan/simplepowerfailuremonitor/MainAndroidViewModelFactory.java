package com.rsargsyan.simplepowerfailuremonitor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MainAndroidViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public MainAndroidViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(application);
    }
}

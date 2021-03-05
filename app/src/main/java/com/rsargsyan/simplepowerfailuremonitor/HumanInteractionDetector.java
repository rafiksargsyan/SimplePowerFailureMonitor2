package com.rsargsyan.simplepowerfailuremonitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

import static android.content.Context.SENSOR_SERVICE;

public class HumanInteractionDetector implements SensorEventListener {
    private static final float ACCEL_THRESHOLD = 0.5f;

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private final OnDetectedCallback onDetectedCallback;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    public HumanInteractionDetector(@NonNull Context context,
                                    @NonNull OnDetectedCallback onDetectedCallback) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        accel = 0.00f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
        this.onDetectedCallback = onDetectedCallback;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] gravity = event.values.clone();
            float x = gravity[0];
            float y = gravity[1];
            float z = gravity[2];
            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = accelCurrent - accelLast;
            accel = accel * 0.9f + delta;
            if (accel > ACCEL_THRESHOLD) {
                onDetectedCallback.onDetected();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public void register() {
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    public interface OnDetectedCallback {
        void onDetected();
    }
}

package com.rsargsyan.simplepowerfailuremonitor.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;

import java.io.IOException;

public class AlarmPlayer {
    private final Context context;
    private MediaPlayer mp;

    public AlarmPlayer(Context context) {
        this.context = context;
    }

    public void play() {
        if (mp == null) {
            initMediaPlayer();
        }
        mp.start();
    }

    public void stop() {
        if (mp != null) {
            mp.reset();
            mp.release();
            mp = null;
        }
    }

    private void initMediaPlayer() {
        try {
            initMediaPlayer1();
        } catch (IOException e) {
            initMediaPlayer2();
        }
    }

    private void initMediaPlayer2() {
        mp = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mp.setLooping(true);
    }

    private void initMediaPlayer1() throws IOException {
        mp = new MediaPlayer();
        mp.setLooping(true);
        mp.setDataSource(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mp.setAudioStreamType(AudioManager.STREAM_ALARM);
        mp.prepare();
    }
}

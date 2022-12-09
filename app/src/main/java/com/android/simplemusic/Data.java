package com.android.simplemusic;

import android.app.Application;
import android.app.NotificationManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;

public class Data extends Application {
    public MediaPlayer mediaPlayer;
    public Equalizer equalizer;
    public NotificationManager nManager;
    public Music curMusic;
    public short curEqSet = -1;
}

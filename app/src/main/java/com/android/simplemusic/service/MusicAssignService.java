package com.android.simplemusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.simplemusic.bean.Music;

import java.util.ArrayList;

public class MusicAssignService extends Service {
    private static final String TAG = "MusicAssignService";
    private ArrayList<Music> musicList;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        musicList = new ArrayList<Music>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

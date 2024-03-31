package com.android.simplemusic.application;

import android.app.Application;

import androidx.room.Room;

import com.android.simplemusic.db.PlaylistDatabase;

public class MainApplication extends Application {
    private static MainApplication mainApplication;
    private PlaylistDatabase playlistDatabase;

    public static MainApplication getInstance() {
        return mainApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        playlistDatabase = Room.databaseBuilder(mainApplication, PlaylistDatabase.class, "Playlist")
                .addMigrations()
                .allowMainThreadQueries()
                .build();
    }

    public PlaylistDatabase getPlaylistDatabase() {
        return playlistDatabase;
    }
}

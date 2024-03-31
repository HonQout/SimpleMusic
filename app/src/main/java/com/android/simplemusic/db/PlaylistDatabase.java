package com.android.simplemusic.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.android.simplemusic.dao.PlaylistDao;
import com.android.simplemusic.entity.Playlist;

@Database(entities = {Playlist.class}, version = 1, exportSchema = false)
public abstract class PlaylistDatabase extends RoomDatabase {
    public abstract PlaylistDao playlistDao();
}
package com.android.simplemusic.room.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.android.simplemusic.room.dao.PlaylistDao;
import com.android.simplemusic.room.entity.Playlist;

@Database(entities = {Playlist.class}, version = 1, exportSchema = false)
public abstract class PlaylistDatabase extends RoomDatabase {
    public abstract PlaylistDao playlistDao();
}
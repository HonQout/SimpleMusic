package com.android.simplemusic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaylistDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Playlist.db";
    private static final int DB_VERSION = 1;

    public PlaylistDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table MyFavorite (" +
                "id integer primary key autoincrement," +
                "name text," +
                "singer text," +
                "path text," +
                "size integer," +
                "duration integer)");
        db.execSQL("create table RecentPlay (" +
                "id integer primary key autoincrement," +
                "name text," +
                "singer text," +
                "path text," +
                "size integer," +
                "duration integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

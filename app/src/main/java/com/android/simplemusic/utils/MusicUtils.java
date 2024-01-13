package com.android.simplemusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.bean.Playlist;
import com.android.simplemusic.definition.Definition;

import java.util.ArrayList;
import java.util.List;

public class MusicUtils {

    private static final String[] mAudioColumn = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.CAPTURE_FRAMERATE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
    };

    public static List<Music> getMusicData(Context context) {
        List<Music> musicList = new ArrayList<Music>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mAudioColumn, null, null, MediaStore.Audio.AudioColumns.TITLE);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                long size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                Music music = new Music(name, artist, path, duration, size);
                if (music.getSize() > 1000 * 800) {
                    musicList.add(music);
                }
            }
            cursor.close();
        }
        return musicList;
    }

    public static List<Playlist> getPlaylistData(PlaylistDBHelper mDBHelper) {
        List<Playlist> playlists = new ArrayList<Playlist>();
        ArrayList<Music> myFavoriteMusic = mDBHelper.query(Definition.PLAYLIST_MY_FAVORITE, "1=1");
        Playlist myFavorite = new Playlist(Definition.PLAYLIST_MY_FAVORITE, myFavoriteMusic);
        playlists.add(myFavorite);
        ArrayList<Music> recentPlayMusic = mDBHelper.query(Definition.PLAYLIST_RECENT_PLAY, "1=1");
        Playlist recentPlay = new Playlist(Definition.PLAYLIST_RECENT_PLAY, recentPlayMusic);
        playlists.add(recentPlay);
        return playlists;
    }

    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            return time / 1000 / 60 + ":0" + time / 1000 % 60;
        } else {
            return time / 1000 / 60 + ":" + time / 1000 % 60;
        }
    }

    public static String formatSize(long size) {
        if (size < 0) {
            return "invalid";
        } else if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return (double) size / 1024 + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return (double) size / 1024 / 1024 + "MB";
        } else if (size < 1024L * 1024 * 1024 * 1024) {
            return (double) size / 1024 / 1024 / 1024 + "GB";
        } else {
            return "too_big";
        }
    }
}
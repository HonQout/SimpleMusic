package com.android.simplemusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.simplemusic.bean.Music;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MusicUtils {
    public static final String TAG = MusicUtils.class.getSimpleName();
    private static final String[] mAudioColumn = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.CAPTURE_FRAMERATE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
    };

    /**
     * Get music data from system MediaStore by using ContentResolver.
     *
     * @param context     The context.
     * @param minDuration The minimum duration (unit:s) of music required.
     * @param minSize     The minimum size (unit:KB) of music required.
     * @return A list contains all music required.
     */
    public static List<Music> getMusicData(@NonNull Context context, int minDuration, long minSize) {
        List<Music> musicList = new ArrayList<Music>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mAudioColumn, null, null, MediaStore.Audio.AudioColumns.TITLE);
        minDuration = minDuration > 0 ? minDuration * 1000 : 0;
        minSize = minSize > 0 ? minSize * 1024 : 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                int bitrate = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE));
                float captureFrameRate = cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.CAPTURE_FRAMERATE));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                Music music = new Music(title, artist, album, bitrate, captureFrameRate, duration, path, size);
                if (music.getDuration() >= minDuration && music.getSize() >= minSize) {
                    musicList.add(music);
                }
            }
            cursor.close();
        }
        return musicList;
    }

    public static String formatBitrate(int bitrate) {
        if (bitrate < 1000) {
            return bitrate + "bps";
        } else if (bitrate < 1000 * 1000) {
            return String.format(Locale.US, "%.1f", (double) bitrate / 1000) + "Kbps";
        } else if (bitrate < 1000 * 1000 * 1000) {
            return String.format(Locale.US, "%.1f", (double) bitrate / 1000 / 1000) + "Mbps";
        } else {
            return String.format(Locale.US, "%.1f", (double) bitrate / 1000 / 1000 / 1000) + "Gbps";
        }
    }

    public static String formatCaptureFrameRate(float captureFrameRate) {
        if (captureFrameRate < 1000) {
            return captureFrameRate + "Hz";
        } else if (captureFrameRate < 1000 * 1000) {
            return String.format(Locale.US, "%.1f", (double) captureFrameRate / 1000) + "kHz";
        } else if (captureFrameRate < 1000 * 1000 * 1000) {
            return String.format(Locale.US, "%.1f", (double) captureFrameRate / 1000 / 1000) + "MHz";
        } else {
            return String.format(Locale.US, "%.1f", (double) captureFrameRate / 1000 / 1000 / 1000) + "GHz";
        }
    }

    public static String formatDuration(int duration) {
        if (duration / 1000 % 60 < 10) {
            return duration / 1000 / 60 + ":0" + duration / 1000 % 60;
        } else {
            return duration / 1000 / 60 + ":" + duration / 1000 % 60;
        }
    }

    public static String formatSize(long size) {
        if (size < 0) {
            return "invalid";
        } else if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format(Locale.US, "%.2f", (double) size / 1024) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.2f", (double) size / 1024 / 1024) + "MB";
        } else if (size < 1024L * 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.2f", (double) size / 1024 / 1024 / 1024) + "GB";
        } else {
            return "infinite";
        }
    }

    @Nullable
    public static Bitmap getAlbumImage(@NonNull String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte[] bytes = retriever.getEmbeddedPicture();
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }
}
package com.android.simplemusic;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MusicService extends Service {

    private static final String TAG = "MusicService";
    private static final int MUSIC_INIT = 0;
    private static final int MUSIC_PLAY = 1;
    private static final int MUSIC_PAUSE = 2;
    private static final int MUSIC_STOP = 3;
    private static final String NOTIFICATION_MUSIC_ID = "Simple_Music";
    private static final int NOTIFICATION_ID = 1024;

    private int currentState;
    private Music currentMusic;
    private MediaPlayer mediaPlayer;
    private MusicBinder musicBinder;
    private NotificationManager nManager;
    private NotificationChannel nChannel;
    private Intent intent;
    private PendingIntent pIntent;
    private Notification notification;

    public MusicService() {

    }

    public class MusicBinder extends Binder {
        public MusicBinder() {

        }

        public void Init(Music music) {
            Log.i(TAG, "Music Init, path is " + music.getPath());
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            currentMusic = music;
            notification = new NotificationCompat.Builder(MusicService.this, NOTIFICATION_MUSIC_ID)
                    .setContentTitle(music.getName())
                    .setContentText(music.getSinger())
                    .setSmallIcon(R.drawable.music_note_black)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pIntent)
                    .build();
            startForeground(NOTIFICATION_ID, notification);
            try {
                mediaPlayer.setDataSource(music.getPath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentState = MUSIC_PLAY;
            Play();
        }

        public void Play() {
            Log.i(TAG, "Music Start");
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                currentState = MUSIC_PLAY;
                mediaPlayer.start();
            }
        }

        public void Pause() {
            Log.i(TAG, "Music Pause");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentState = MUSIC_PAUSE;
                mediaPlayer.pause();
            }
        }

        public void Stop() {
            Log.i(TAG, "Music Stop");
            if (mediaPlayer != null) {
                currentState = MUSIC_STOP;
                mediaPlayer.stop();
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public Music getCurrentMusic() {
            return currentMusic;
        }

        public int getCurrentState() {
            return currentState;
        }

        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        public int getCurrentPosition() {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            } else {
                return -1;
            }
        }

        public int getDuration() {
            return mediaPlayer.getDuration();
        }

        public void setPosition(int position) {
            if (mediaPlayer != null && position > 0) {
                mediaPlayer.seekTo(position);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void onCreate() {
        Log.i(TAG, "Create Service");
        mediaPlayer = new MediaPlayer();
        musicBinder = new MusicBinder();
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nChannel = new NotificationChannel(NOTIFICATION_MUSIC_ID, getString(R.string.music_channel), NotificationManager.IMPORTANCE_DEFAULT);
            nManager.createNotificationChannel(nChannel);
        }
        intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification = new NotificationCompat.Builder(this, NOTIFICATION_MUSIC_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.not_playing))
                .setSmallIcon(R.drawable.music_note_black)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pIntent)
                .addAction(R.drawable.prev_black, getString(R.string.action_prev), null)
                .addAction(R.drawable.play_black, getString(R.string.action_play), null)
                .addAction(R.drawable.next_black, getString(R.string.action_next), null)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Start Service");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int option = bundle.getInt("option");
                String name = bundle.getString("name");
                String singer = bundle.getString("singer");
                String path = bundle.getString("path");
                int duration = bundle.getInt("duration");
                long size = bundle.getLong("size");
                currentMusic = new Music(name, singer, path, duration, size);
                switch (option) {
                    case MUSIC_INIT:
                        Log.i(TAG, "Music Init, path is " + currentMusic.getPath());
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                        } else {
                            mediaPlayer.reset();
                        }
                        try {
                            mediaPlayer.setDataSource(path);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MUSIC_PLAY:
                        Log.i(TAG, "Music Start");
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                        break;
                    case MUSIC_PAUSE:
                        Log.i(TAG, "Music Pause");
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        break;
                    case MUSIC_STOP:
                        Log.i(TAG, "Music Stop");
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            try {
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return flags;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy Service");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
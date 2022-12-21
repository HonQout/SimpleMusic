package com.android.simplemusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

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
    private Equalizer equalizer;
    private static short savedEqPreset;
    private static ArrayList<Short> savedEqSettings;
    private NotificationManager nManager;
    private NotificationChannel nChannel;
    private Intent intent;
    private PendingIntent pIntent;
    private Notification notification;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(true);
        musicBinder = new MusicBinder();
        savedEqPreset = -1;
        savedEqSettings = new ArrayList<Short>();
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nChannel = new NotificationChannel(NOTIFICATION_MUSIC_ID, getString(R.string.music_channel), NotificationManager.IMPORTANCE_DEFAULT);
            nManager.createNotificationChannel(nChannel);
        }
        intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
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

    public class MusicBinder extends Binder {

        public void Init(Music music) {
            Log.i(TAG, "Music Init, path is " + music.getPath());
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            currentMusic = music;
            Log.i(TAG, "Equalizer Init");
            if (equalizer == null) {
                equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
                equalizer.setEnabled(true);
            }
            if (savedEqPreset != -1) {
                equalizer.usePreset(savedEqPreset);
            } else {
                equalizer.usePreset((short) 0);
            }
            Log.i(TAG, "Use saved preset " + equalizer.getPresetName(savedEqPreset) + " to initialize equalizer");
            if (savedEqSettings.size() == equalizer.getNumberOfBands()) {
                for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                    equalizer.setBandLevel(i, savedEqSettings.get((int) i));
                }
                Log.i(TAG, "Use saved settings to initialize equalizer");
            }
            notification = new NotificationCompat.Builder(MusicService.this, NOTIFICATION_MUSIC_ID)
                    .setContentTitle(music.getName())
                    .setContentText(music.getSinger())
                    .setSmallIcon(R.drawable.music_note_black)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.prev_black, getString(R.string.action_prev), null)
                    .addAction(R.drawable.play_black, getString(R.string.action_play), null)
                    .addAction(R.drawable.next_black, getString(R.string.action_next), null)
                    .build();
            startForeground(NOTIFICATION_ID, notification);
            try {
                mediaPlayer.setDataSource(music.getPath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        public boolean isLooping() {
            return mediaPlayer.isLooping();
        }

        public void setCycleMode() {
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(!mediaPlayer.isLooping());
            }
        }

        public short getNumBands() {
            return equalizer.getNumberOfBands();
        }

        public short getMinBandLevel() {
            return equalizer.getBandLevelRange()[0];
        }

        public short getMaxBandLevel() {
            return equalizer.getBandLevelRange()[1];
        }

        public short getBandLevel(short band) {
            return equalizer.getBandLevel(band);
        }

        public void setBandLevel(short band, short level) {
            equalizer.setBandLevel(band, level);
            savedEqSettings = new ArrayList<Short>();
            for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                savedEqSettings.add(equalizer.getBandLevel(i));
            }
        }

        public int getCenterFreq(short band) {
            return equalizer.getCenterFreq(band);
        }

        public short getNumPresets() {
            return equalizer.getNumberOfPresets();
        }

        public String getPresetName(short preset) {
            return equalizer.getPresetName(preset);
        }

        public ArrayList<String> getAllPresets() {
            ArrayList<String> allPresets = new ArrayList<String>();
            for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
                allPresets.add(equalizer.getPresetName(i));
            }
            return allPresets;
        }

        public short getCurrentPreset() {
            return savedEqPreset;
        }

        public ArrayList<Short> getSavedEqSettings() {
            return savedEqSettings;
        }

        public void usePreset(short preset) {
            if (savedEqPreset == -1 || savedEqPreset != preset) {
                equalizer.usePreset(preset);
            }
            savedEqPreset = preset;
            Log.i(TAG, "Use preset " + equalizer.getPresetName(preset));
        }

        public void resetEqSet() {
            if (savedEqPreset == -1) {
                savedEqPreset = (short) Equalizer.CONTENT_TYPE_MUSIC;
            }
            equalizer.usePreset(savedEqPreset);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (equalizer != null) {
            equalizer.release();
        }
    }
}
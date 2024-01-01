package com.android.simplemusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.simplemusic.activity.MainActivity;
import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.intent.MusicIntent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private static final String NOTIFICATION_MUSIC_ID = "Simple_Music";
    private static final int NOTIFICATION_ID = 1024;

    private String currentState;
    private Music currentMusic;
    private MediaPlayer mediaPlayer;
    private final MusicBinder mBinder = new MusicBinder();
    private Equalizer equalizer;
    private static short savedEqPreset;
    private static ArrayList<Short> savedEqSettings;
    private NotificationManager nManager;
    private NotificationChannel nChannel;
    private Intent intent;
    private PendingIntent pIntent;
    private Notification notification;
    private PlaylistDBHelper mHelper;
    private MusicControlReceiver mcr;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        // 注册EventBus
        EventBus.getDefault().register(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                EventBus.getDefault().post(new MessageEvent(Definition.COMPLETION));
            }
        });
        equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(true);
        savedEqPreset = -1;
        savedEqSettings = new ArrayList<Short>();
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nChannel = new NotificationChannel(NOTIFICATION_MUSIC_ID, getString(R.string.music_channel), NotificationManager.IMPORTANCE_DEFAULT);
            nManager.createNotificationChannel(nChannel);
        }
        intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        mcr = new MusicControlReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Definition.PREV);
        intentFilter.addAction(Definition.PLAY_PAUSE);
        intentFilter.addAction(Definition.NEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mcr, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mcr, intentFilter);
        }
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

    public void Init(Music music) {
        Log.i(TAG, "Music Init + Path: " + music.getPath());
        currentState = Definition.INIT;
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
        // 注册待定意图
        PendingIntent piPrev = PendingIntent.getBroadcast(this, 0, MusicIntent.intentPrev, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent piPlayPause = PendingIntent.getBroadcast(this, 0, MusicIntent.intentPlayPause, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent piNext = PendingIntent.getBroadcast(this, 0, MusicIntent.intentNext, PendingIntent.FLAG_IMMUTABLE);
        // 注册广播接收器
        notification = new NotificationCompat.Builder(MusicService.this, NOTIFICATION_MUSIC_ID)
                .setContentTitle(music.getTitle())
                .setContentText(music.getArtist())
                .setSmallIcon(R.drawable.music_note_black)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pIntent)
                .addAction(R.drawable.prev_black, getString(R.string.action_prev), piPrev)
                .addAction(R.drawable.play_black, getString(R.string.action_play), piPlayPause)
                .addAction(R.drawable.next_black, getString(R.string.action_next), piNext)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        try {
            mediaPlayer.setDataSource(music.getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new MessageEvent(Definition.INIT));
        Play();
    }

    public void Play() {
        Log.i(TAG, "Music Play");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            currentState = Definition.PLAY;
            mediaPlayer.start();
            EventBus.getDefault().post(new MessageEvent(Definition.PLAY));
        }
    }

    public void Pause() {
        Log.i(TAG, "Music Pause");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentState = Definition.PAUSE;
            mediaPlayer.pause();
            EventBus.getDefault().post(new MessageEvent(Definition.PAUSE));
        }
    }

    public void Play_Pause() {
        Log.i(TAG, "Music Play_Pause");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                currentState = Definition.PAUSE;
                mediaPlayer.pause();
                EventBus.getDefault().post(new MessageEvent(Definition.PAUSE));
            } else {
                currentState = Definition.PLAY;
                mediaPlayer.start();
                EventBus.getDefault().post(new MessageEvent(Definition.PLAY));
            }
        }
    }

    public void Stop() {
        Log.i(TAG, "Music Stop");
        if (mediaPlayer != null) {
            currentState = Definition.STOP;
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EventBus.getDefault().post(new MessageEvent(Definition.STOP));
        }
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public String getCurrentState() {
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
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return -1;
        }
    }

    public boolean setPosition(int position) {
        if (mediaPlayer != null && position >= 0 && position < mediaPlayer.getDuration()) {
            mediaPlayer.seekTo(position);
            return true;
        } else {
            return false;
        }
    }

    public boolean isLooping() {
        if (mediaPlayer != null) {
            return mediaPlayer.isLooping();
        } else {
            return false;
        }
    }

    public void setCycleMode() {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(!mediaPlayer.isLooping());
            EventBus.getDefault().post(new MessageEvent(mediaPlayer.isLooping() ?
                    Definition.REPEAT_ALL : Definition.NOT_REPEATING));
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

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
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
        if (mcr != null) {
            unregisterReceiver(mcr);
        }
    }

    // 音乐播放控制广播接收器
    public class MusicControlReceiver extends BroadcastReceiver {
        public static final String TAG = "MusicControlReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Definition.PREV:
                        Log.i(TAG, "Received PREV");
                        break;
                    case Definition.PLAY_PAUSE:
                        Log.i(TAG, "Received PLAY_PAUSE");
                        Play_Pause();
                        break;
                    case Definition.NEXT:
                        Log.i(TAG, "Received NEXT");
                        break;
                    case Definition.STOP:
                        Log.i(TAG, "Received STOP");
                        break;
                    case Definition.CYCLE_CHANGE:
                        Log.i(TAG, "Received CYCLE_CHANGE");
                        setCycleMode();
                        break;
                    default:
                        Log.i(TAG, String.format("Received %s", action));
                        break;
                }
            }
        }
    }

    // 处理EventBus事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String eventMessage = messageEvent.getMessage();
        switch (eventMessage) {
            case Definition.PREV:
                Log.i(TAG, "Received PREV");
                break;
            case Definition.PLAY_PAUSE:
                Log.i(TAG, "Received PLAY_PAUSE");
                Play_Pause();
                break;
            case Definition.NEXT:
                Log.i(TAG, "Received NEXT");
                break;
            case Definition.STOP:
                Log.i(TAG, "Received STOP");
                break;
            case Definition.CYCLE_CHANGE:
                Log.i(TAG, "Received CYCLE_CHANGE");
                setCycleMode();
                break;
            default:
                Log.i(TAG, String.format("Received %s", eventMessage));
                break;
        }
    }
}
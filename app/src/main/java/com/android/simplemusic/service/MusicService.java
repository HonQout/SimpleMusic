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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.simplemusic.R;
import com.android.simplemusic.activity.MainActivity;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.bean.MyEqualizer;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.utils.FileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

public class MusicService extends Service {
    private static final String TAG = MusicService.class.getSimpleName();
    private static final String NOTIFICATION_MUSIC_ID = "Simple_Music";
    private static final int NOTIFICATION_ID = 1;
    public static final Intent intentPrev = new Intent(Definition.PREV);
    public static final Intent intentPlayPause = new Intent(Definition.PLAY_PAUSE);
    public static final Intent intentNext = new Intent(Definition.NEXT);
    public static final Intent intentStop = new Intent(Definition.STOP);

    private String currentState;
    private Music currentMusic = null;
    private MediaPlayer mediaPlayer;
    private final MusicBinder mBinder = new MusicBinder();
    private MyEqualizer equalizer;
    private NotificationManager nManager;
    private NotificationChannel nChannel;
    private PendingIntent pIntent;
    private Notification notification;
    private MusicControlReceiver mcr;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 初始化mediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                EventBus.getDefault().post(new MessageEvent(Definition.COMPLETION));
            }
        });
        // 初始化equalizer
        equalizer = new MyEqualizer(0, mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(true);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nChannel = new NotificationChannel(NOTIFICATION_MUSIC_ID, getString(R.string.music_channel), NotificationManager.IMPORTANCE_DEFAULT);
            nManager.createNotificationChannel(nChannel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        mcr = new MusicControlReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Definition.PREV);
        intentFilter.addAction(Definition.PLAY_PAUSE);
        intentFilter.addAction(Definition.NEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mcr, intentFilter, Context.RECEIVER_NOT_EXPORTED);
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

    public boolean Init(Music music) {
        if (!FileUtils.exists(music.getPath())) {
            Log.i(TAG, "Music doesn't exist.");
            return false;
        }
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
            equalizer = new MyEqualizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);
        }
        equalizer.init();
        // 注册待定意图
        PendingIntent piPrev = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent piPlayPause = PendingIntent.getBroadcast(this, 0, intentPlayPause, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent piNext = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_IMMUTABLE);
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
        return true;
    }

    public void Play() {
        Log.i(TAG, "Play");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            currentState = Definition.PLAY;
            mediaPlayer.start();
            EventBus.getDefault().post(new MessageEvent(Definition.PLAY));
        }
    }

    public void Pause() {
        Log.i(TAG, "Pause");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentState = Definition.PAUSE;
            mediaPlayer.pause();
            EventBus.getDefault().post(new MessageEvent(Definition.PAUSE));
        }
    }

    public void Play_Pause() {
        Log.i(TAG, "Play_Pause");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                Pause();
            } else {
                Play();
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

    public void changeCycleMode() {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(!mediaPlayer.isLooping());
            EventBus.getDefault().post(new MessageEvent(mediaPlayer.isLooping() ?
                    Definition.REPEAT_ALL : Definition.NOT_REPEATING));
        }
    }

    public MyEqualizer getEqualizer() {
        return equalizer;
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
                        changeCycleMode();
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
        String message = messageEvent.getMessage();
        if (message != null) {
            Log.i(TAG, "Received EventBus message " + message);
            switch (message) {
                case Definition.PERMISSION_ACQUIRED:
                    break;
                case Definition.PREV:
                    break;
                case Definition.PLAY_PAUSE:
                    Play_Pause();
                    break;
                case Definition.NEXT:
                    break;
                case Definition.STOP:
                    break;
                case Definition.CYCLE_CHANGE:
                    changeCycleMode();
                    break;
                default:
                    break;
            }
        }
    }
}
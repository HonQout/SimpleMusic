package com.android.simplemusic.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.databinding.ActivityPlayingBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.utils.ColorUtils;
import com.android.simplemusic.utils.MusicUtils;
import com.android.simplemusic.utils.ThemeUtils;
import com.android.simplemusic.view.dialog.EqualizerDialog;
import com.android.simplemusic.viewmodel.MainViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PlayingActivity extends AppCompatActivity {
    private static final String TAG = "PlayingActivity";
    private ActivityPlayingBinding binding;
    private boolean shouldUpdateProgress = true;
    private MainViewModel viewModel;
    private MusicService musicService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            musicService = ((MusicService.MusicBinder) service).getService();
            EventBus.getDefault().post(new MessageEvent(Definition.SERVICE_CONNECTED, PlayingActivity.class.getSimpleName()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            musicService = null;
            EventBus.getDefault().post(new MessageEvent(Definition.SERVICE_DISCONNECTED, PlayingActivity.class.getSimpleName()));
        }
    };
    private ThemeUtils themeUtils;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 初始化界面
        binding = ActivityPlayingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        themeUtils = new ThemeUtils(this) {
            @Override
            public void whenEnabledNightMode() {
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
                binding.getRoot().setBackgroundColor(Color.BLACK);
            }

            @Override
            public void whenDisabledNightMode() {
                int color = ColorUtils.analyzeColor(PlayingActivity.this,
                        sharedPreferences.getString("theme_color", "white"));
                if (sharedPreferences.getBoolean("immersion_status_bar", true)) {
                    getWindow().setStatusBarColor(color);
                } else {
                    getWindow().setStatusBarColor(Color.GRAY);
                }
                if (sharedPreferences.getBoolean("immersion_navigation_bar", true)) {
                    getWindow().setNavigationBarColor(color);
                } else {
                    getWindow().setNavigationBarColor(Color.TRANSPARENT);
                }
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                binding.getRoot().setBackgroundColor(color);
                binding.sbProgress.setBackgroundColor(ColorUtils.getInverseColor(color));
            }
        };
        // 绑定ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getImage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                if (bitmap != null) {
                    binding.musicPic.setImageBitmap(bitmap);
                } else {
                    binding.musicPic.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.record, null));
                }
            }
        });
        if (viewModel.getImage().getValue() != null) {
            binding.musicPic.setImageBitmap(viewModel.getImage().getValue());
        } else {
            binding.musicPic.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.record, null));
        }
        // 绑定Music Service
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        // 设置控件的事件侦听器
        if (viewModel.getImage().getValue() != null) {
            binding.musicPic.setImageBitmap(viewModel.getImage().getValue());
        } else {
            binding.musicPic.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.record, null));
        }
        //binding.barGraph.setNumBar(20);
        binding.sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.i(TAG, "progress:" + progress + ",fromUser:" + fromUser);
                if (fromUser && musicService.getCurrentMusic() != null) {
                    musicService.setPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                shouldUpdateProgress = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                shouldUpdateProgress = true;
            }
        });
        binding.ibCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.CYCLE_CHANGE));
            }
        });
        binding.ibPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.PREV));
            }
        });
        binding.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new MessageEvent(Definition.PLAY_PAUSE));
            }
        });
        binding.ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.NEXT));
            }
        });
        binding.ibEq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new EqualizerDialog(PlayingActivity.this) {

                    @Override
                    public void onSeekBarChange(short band, short level) {
                        Log.i("Equalizer", String.format("set band %d to %d", (int) band, (int) level));
                        musicService.getEqualizer().setBandLevel(band, level);
                    }

                    @Override
                    public void onSpinnerChange(int position, ArrayList<SeekBar> seekBars, int minBandLevel) {
                        Log.i(TAG, String.format("Selected preset item %d", position));
                        musicService.getEqualizer().usePreset((short) position);
                        for (short i = 0; i < musicService.getEqualizer().getNumberOfBands(); i++) {
                            seekBars.get((int) i).setProgress(musicService.getEqualizer().getBandLevel(i) - minBandLevel);
                        }
                        Log.i(TAG, String.format("Not first use, set preset %d", position));
                    }

                    @Override
                    public void onResetClick(ArrayList<SeekBar> seekBars, int minBandLevel) {
                        musicService.getEqualizer().reset();
                        for (short i = 0; i < musicService.getEqualizer().getNumberOfBands(); i++) {
                            seekBars.get((int) i).setProgress(musicService.getEqualizer().getBandLevel(i) - minBandLevel);
                        }
                    }
                }
                        .setPresets(musicService.getEqualizer().getPresetNames())
                        .setSelection(musicService.getEqualizer().getCurrentPreset())
                        .setNumBands(musicService.getEqualizer().getNumberOfBands())
                        .setMinBandLevel(musicService.getEqualizer().getMinBandLevel())
                        .setCenterFrequencies(musicService.getEqualizer().getCenterFrequencies())
                        .setMaxBandLevel(musicService.getEqualizer().getMaxBandLevel())
                        .create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        viewModel.getImage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                if (bitmap != null) {
                    binding.musicPic.setImageBitmap(bitmap);
                } else {
                    binding.musicPic.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.record, null));
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    // 处理EventBus事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String eventMessage = messageEvent.getMessage();
        switch (eventMessage) {
            case Definition.SERVICE_CONNECTED:
                if (messageEvent.getContent().equals("PlayingActivity")) {
                    if (musicService.getCurrentMusic() != null) {
                        Music currentMusic = musicService.getCurrentMusic();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.tvName.setText(currentMusic.getTitle());
                                binding.tvArtist.setText(currentMusic.getArtist());
                                binding.sbProgress.setMax(musicService.getDuration());
                                binding.tvCurrentTime.setText(MusicUtils.formatDuration(musicService.getCurrentPosition()));
                                binding.tvTotalTime.setText(MusicUtils.formatDuration(musicService.getDuration()));
                                binding.ibCycle.setImageResource(musicService.isLooping() ? R.drawable.baseline_repeat_one_white : R.drawable.baseline_repeat_white);
                                binding.ibPlay.setImageResource(musicService.isPlaying() ? R.drawable.baseline_pause_circle_outline_white : R.drawable.baseline_play_circle_outline_white);
                            }
                        });
                    }
                    timer = new Timer();
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (musicService.getCurrentMusic() != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.tvCurrentTime.setText(MusicUtils.formatDuration(musicService.getCurrentPosition()));
                                        if (shouldUpdateProgress) {
                                            binding.sbProgress.setProgress(musicService.getCurrentPosition());
                                        }
                                    }
                                });
                            }
                        }
                    };
                    timer.schedule(timerTask, 0, 100);
                }
                break;
            case Definition.INIT:
                Log.i(TAG, "Handled Message INIT");
                binding.tvName.setText(musicService.getCurrentMusic().getTitle());
                binding.tvArtist.setText(musicService.getCurrentMusic().getArtist());
                binding.sbProgress.setMax(musicService.getCurrentMusic().getDuration());
                binding.tvTotalTime.setText(MusicUtils.formatDuration(musicService.getCurrentMusic().getDuration()));
                break;
            case Definition.PLAY:
                Log.i(TAG, "Received PLAY");
                binding.ibPlay.setImageResource(R.drawable.baseline_pause_circle_outline_white);
                break;
            case Definition.PAUSE:
                Log.i(TAG, "Received PAUSE");
                binding.ibPlay.setImageResource(R.drawable.baseline_play_circle_outline_white);
                break;
            case Definition.COMPLETION:
                Log.i(TAG, "Received COMPLETION");
                if (!musicService.isPlaying()) {
                    binding.ibPlay.setImageResource(R.drawable.baseline_play_circle_outline_white);
                }
                break;
            case Definition.NOT_REPEATING:
                Log.i(TAG, "Received NOT_REPEATING");
                binding.ibCycle.setImageResource(R.drawable.baseline_repeat_white);
                break;
            case Definition.REPEAT_ALL:
                Log.i(TAG, "Received REPEAT_ALL");
                binding.ibCycle.setImageResource(R.drawable.baseline_repeat_one_white);
                break;
            case Definition.PROGRESS_CHANGE:
                Log.i(TAG, "Received PROGRESS_CHANGE");
                binding.tvCurrentTime.setText(MusicUtils.formatDuration(musicService.getCurrentPosition()));
                if (shouldUpdateProgress) {
                    binding.sbProgress.setProgress(musicService.getCurrentPosition());
                }
                break;
            case Definition.VISUALIZER_DATA_UPDATE:
                Log.i(TAG, "Received " + Definition.VISUALIZER_DATA_UPDATE);
                String info = (String) messageEvent.getContent();
                if (info.equals("WaveForm")) {
                    //binding.barGraph.onReceiveByte(musicService.getWaveformData());
                } else if (info.equals("Fft")) {
                    //binding.barGraph.onReceiveByte(musicService.getFftData());
                }
                break;
            default:
                break;
        }
    }
}
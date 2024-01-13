package com.android.simplemusic.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.simplemusic.bean.Music;
import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.R;
import com.android.simplemusic.databinding.ActivityPlayingBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.utils.MusicUtils;
import com.android.simplemusic.vm.MainViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PlayingActivity extends AppCompatActivity {
    private static final String TAG = "PlayingActivity";
    private ActivityPlayingBinding binding;
    private int UiMode;
    private boolean shouldUpdateProgress = true;
    private MainViewModel model;
    private MusicService musicService;
    private PlaylistDBHelper mDBHelper;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            musicService = ((MusicService.MusicBinder) service).getService();
            EventBus.getDefault().post(new MessageEvent(Definition.SERVICE_CONNECTED, "PlayingActivity"));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            musicService = null;
        }
    };
    private Timer timer;
    private TimerTask timerTask;
    private TextView tv_name;
    private TextView tv_artist;
    private SeekBar sb_progress;
    private TextView tv_currentTime;
    private TextView tv_totalTime;
    private ImageButton ib_cycle;
    private ImageButton ib_prev;
    private ImageButton ib_play;
    private ImageButton ib_next;
    private ImageButton ib_eq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 获取绑定
        binding = ActivityPlayingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 设置系统UI可见性
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // 初始化Toolbar
        binding.toolbarPlaying.setTitle("");
        setSupportActionBar(binding.toolbarPlaying);
        // 绑定ViewModel
        model = new ViewModelProvider(this).get(MainViewModel.class);
        // 绑定Music Service
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        // 初始化控件
        View root = binding.getRoot();
        tv_name = root.findViewById(R.id.tv_name);
        tv_artist = root.findViewById(R.id.tv_artist);
        sb_progress = root.findViewById(R.id.sb_progress);
        tv_currentTime = root.findViewById(R.id.tv_current_time);
        tv_totalTime = root.findViewById(R.id.tv_total_time);
        ib_cycle = root.findViewById(R.id.ib_cycle);
        ib_prev = root.findViewById(R.id.ib_prev);
        ib_play = root.findViewById(R.id.ib_play);
        ib_next = root.findViewById(R.id.ib_next);
        ib_eq = root.findViewById(R.id.ib_eq);
        // 设置控件的事件侦听器
        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "progress:" + progress + ",fromUser:" + fromUser);
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
        ib_cycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.CYCLE_CHANGE));
            }
        });
        ib_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new MessageEvent(Definition.PLAY_PAUSE));
            }
        });
        ib_eq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder eqdBuilder = new AlertDialog.Builder(PlayingActivity.this);
                eqdBuilder.setIcon(R.drawable.equalizer_black);
                eqdBuilder.setTitle(getString(R.string.equalizer));
                LinearLayout linearLayout = new LinearLayout(PlayingActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                // “使用预设”行
                View eq_top = View.inflate(PlayingActivity.this, R.layout.equalizer_top, null);
                TextView textView_ps = eq_top.findViewById(R.id.equalizer_top_hint);
                Spinner spinner_ps = eq_top.findViewById(R.id.equalizer_top_spinner);
                textView_ps.setText(R.string.use_preset);
                ArrayList<String> eqPresets = musicService.getAllPresets();
                ArrayAdapter<String> eqSpAdapter = new ArrayAdapter<String>(PlayingActivity.this, R.layout.spinner_item_select, eqPresets);
                eqSpAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
                spinner_ps.setAdapter(eqSpAdapter);
                spinner_ps.setSelection(musicService.getCurrentPreset());
                Log.i(TAG, "Current Preset is " + String.valueOf(musicService.getCurrentPreset()));
                linearLayout.addView(eq_top);
                // 获取Equalizer
                final short minEqualizer = musicService.getMinBandLevel();
                final short maxEqualizer = musicService.getMaxBandLevel();
                ArrayList<SeekBar> seekBars = new ArrayList<SeekBar>();
                for (short i = 0; i < musicService.getNumBands(); i++) {
                    final short band = i;
                    View eq_unit = View.inflate(PlayingActivity.this, R.layout.equalizer_unit, null);
                    TextView textView1 = eq_unit.findViewById(R.id.equalizer_unit_minEq);
                    TextView textView2 = eq_unit.findViewById(R.id.equalizer_unit_freq);
                    TextView textView3 = eq_unit.findViewById(R.id.equalizer_unit_maxEq);
                    SeekBar seekBar = eq_unit.findViewById(R.id.equalizer_unit_seekbar);
                    textView1.setText(String.format(Locale.US, "%ddB", minEqualizer / 100));
                    textView2.setText(String.format(Locale.US, "%dHz", musicService.getCenterFreq(band) / 1000));
                    textView3.setText(String.format(Locale.US, "%ddB", maxEqualizer / 100));
                    seekBar.setMax((int) (maxEqualizer - minEqualizer));
                    Log.i("Equalizer", String.format("band %d is %d", (int) band, (int) musicService.getBandLevel(band)));
                    seekBar.setProgress(musicService.getBandLevel(band) - minEqualizer);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }

                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                Log.i("Equalizer", String.format("set band %d to %d + %d", (int) band, (int) progress, (int) minEqualizer));
                                musicService.setBandLevel(band, (short) (progress + minEqualizer));
                            }
                        }
                    });
                    seekBars.add(seekBar);
                    linearLayout.addView(eq_unit);
                }
                spinner_ps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.i(TAG, String.format("Selected preset item %d", position));
                        musicService.usePreset((short) position);
                        for (short i = 0; i < musicService.getNumBands(); i++) {
                            seekBars.get((int) i).setProgress(musicService.getBandLevel(i) - minEqualizer);
                        }
                        Log.i(TAG, String.format("Not first use, set preset %d", position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                eqdBuilder.setView(linearLayout);
                eqdBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                eqdBuilder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        musicService.resetEqSet();
                        Log.i("Equalizer", String.format("Number of presets is %d", (int) musicService.getNumPresets()));
                        for (short i = 0; i < musicService.getNumBands(); i++) {
                            seekBars.get((int) i).setProgress(musicService.getBandLevel(i) - minEqualizer);
                        }
                    }
                });
                AlertDialog eqDialog = eqdBuilder.create();
                eqDialog.show();
                eqDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        musicService.resetEqSet();
                        Log.i("Equalizer", String.format("Number of presets is %d", (int) musicService.getNumPresets()));
                        for (short i = 0; i < musicService.getNumBands(); i++) {
                            seekBars.get((int) i).setProgress(musicService.getBandLevel(i) - minEqualizer);
                        }
                    }
                });
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
        mDBHelper = PlaylistDBHelper.getInstance(this, 1);
        model.setmDBHelper(mDBHelper);
        mDBHelper.openWriteLink();
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
                                tv_name.setText(currentMusic.getTitle());
                                tv_artist.setText(currentMusic.getArtist());
                                sb_progress.setMax(musicService.getDuration());
                                tv_currentTime.setText(MusicUtils.formatTime(musicService.getCurrentPosition()));
                                tv_totalTime.setText(MusicUtils.formatTime(musicService.getDuration()));
                                ib_cycle.setImageResource(musicService.isLooping() ? R.drawable.repeat_all_black : R.drawable.arrow_right_black);
                                ib_play.setImageResource(musicService.isPlaying() ? R.drawable.pause_black : R.drawable.play_black);
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
                                        tv_currentTime.setText(MusicUtils.formatTime(musicService.getCurrentPosition()));
                                        if (shouldUpdateProgress) {
                                            sb_progress.setProgress(musicService.getCurrentPosition());
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
                tv_name.setText(musicService.getCurrentMusic().getTitle());
                tv_artist.setText(musicService.getCurrentMusic().getArtist());
                sb_progress.setMax(musicService.getCurrentMusic().getDuration());
                tv_totalTime.setText(MusicUtils.formatTime(musicService.getCurrentMusic().getDuration()));
                break;
            case Definition.PLAY:
                Log.i(TAG, "Received PLAY");
                ib_play.setImageResource(R.drawable.pause_black);
                break;
            case Definition.PAUSE:
                Log.i(TAG, "Received PAUSE");
                ib_play.setImageResource(R.drawable.play_black);
                break;
            case Definition.COMPLETION:
                Log.i(TAG, "Received COMPLETION");
                if (!musicService.isPlaying()) {
                    ib_play.setImageResource(R.drawable.play_black);
                }
                break;
            case Definition.NOT_REPEATING:
                Log.i(TAG, "Received NOT_REPEATING");
                ib_cycle.setImageResource(R.drawable.arrow_right_black);
                break;
            case Definition.REPEAT_ALL:
                Log.i(TAG, "Received REPEAT_ALL");
                ib_cycle.setImageResource(R.drawable.repeat_all_black);
                break;
            case Definition.PROGRESS_CHANGE:
                Log.i(TAG, "Received PROGRESS_CHANGE");
                tv_currentTime.setText(MusicUtils.formatTime(musicService.getCurrentPosition()));
                if (shouldUpdateProgress) {
                    sb_progress.setProgress(musicService.getCurrentPosition());
                }
                break;
            default:
                break;
        }
    }
}
package com.android.simplemusic.ui.nowplaying;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.Music;
import com.android.simplemusic.MusicService;
import com.android.simplemusic.MusicUtils;
import com.android.simplemusic.R;
import com.android.simplemusic.databinding.FragmentNowplayingBinding;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NowPlayingFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ServiceConnection {
    private static final String TAG = "NowPlayingFragment";

    private FragmentNowplayingBinding binding;
    private TextView NP_songName;
    private TextView NP_songSinger;
    private TextView NP_cTime;
    private SeekBar NP_seekbar;
    private TextView NP_tTime;
    private ImageButton NP_cycle;
    private ImageButton NP_eq;
    private ImageButton NP_prev;
    private ImageButton NP_play;
    private ImageButton NP_next;
    private ImageButton NP_playlist;
    private Timer timer;
    private Music prevMusic;
    private MusicService.MusicBinder musicBinder;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(requireActivity().getApplicationContext(), MusicService.class);
        requireActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        NowPlayingViewModel nowPlayingViewModel =
                new ViewModelProvider(this).get(NowPlayingViewModel.class);
        binding = FragmentNowplayingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 初始化控件
        NP_songName = root.findViewById(R.id.NP_songName);
        NP_songSinger = root.findViewById(R.id.NP_songSinger);
        NP_cTime = root.findViewById(R.id.NP_cTime);
        NP_seekbar = root.findViewById(R.id.NP_seekbar);
        NP_tTime = root.findViewById(R.id.NP_tTime);
        NP_cycle = root.findViewById(R.id.NP_cycle);
        NP_eq = root.findViewById(R.id.NP_eq);
        NP_prev = root.findViewById(R.id.NP_prev);
        NP_play = root.findViewById(R.id.NP_play);
        NP_next = root.findViewById(R.id.NP_next);
        NP_playlist = root.findViewById(R.id.NP_playlist);
        NP_seekbar.setOnSeekBarChangeListener(this);
        timer = new Timer();
        timer.schedule(new MyTask(), 0, 200);
        NP_cycle.setImageResource(R.drawable.repeat_all_black);
        NP_cycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicBinder.setCycleMode();
                if (isAdded()) {
                    if (musicBinder.isLooping()) {
                        NP_cycle.setImageResource(R.drawable.repeat_one_black);
                    } else {
                        NP_cycle.setImageResource(R.drawable.repeat_all_black);
                    }
                }
            }
        });
        NP_eq.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                AlertDialog.Builder eqDialog = new AlertDialog.Builder(requireActivity());
                eqDialog.setIcon(R.drawable.equalizer_black);
                eqDialog.setTitle(getString(R.string.equalizer));
                LinearLayout linearLayout = new LinearLayout(requireActivity().getApplicationContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                // “使用预设”提示
                LinearLayout linearLayout_ps = new LinearLayout(requireActivity().getApplicationContext());
                LinearLayout.LayoutParams lp_ps = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp_ps.setMargins(20, 10, 20, 10);
                linearLayout_ps.setLayoutParams(lp_ps);
                linearLayout_ps.setOrientation(LinearLayout.HORIZONTAL);
                TextView textView_ps = new TextView(requireActivity().getApplicationContext());
                LinearLayout.LayoutParams lp_ps_tv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp_ps_tv.setMargins(0, 0, 20, 0);
                textView_ps.setLayoutParams(lp_ps_tv);
                textView_ps.setText(R.string.use_preset);
                textView_ps.setTextSize(16);
                linearLayout_ps.addView(textView_ps);
                // 预设下拉列表
                Spinner spinner_ps = new Spinner(requireActivity().getApplicationContext());
                spinner_ps.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                ArrayList<String> eqPresets = musicBinder.getAllPresets();
                ArrayAdapter<String> eqSpAdapter = new ArrayAdapter<String>(requireActivity().getApplicationContext(), R.layout.spinner_item_select, eqPresets);
                eqSpAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
                spinner_ps.setAdapter(eqSpAdapter);
                spinner_ps.setSelection(musicBinder.getCurrentPreset());
                Log.i(TAG, "Current Preset is " + String.valueOf(musicBinder.getCurrentPreset()));
                linearLayout_ps.addView(spinner_ps);
                linearLayout.addView(linearLayout_ps);
                // 自定义区域
                // 获取Equalizer
                final short minEqualizer = musicBinder.getMinBandLevel();
                final short maxEqualizer = musicBinder.getMaxBandLevel();
                ArrayList<SeekBar> seekBars = new ArrayList<SeekBar>();
                for (short i = 0; i < musicBinder.getNumBands(); i++) {
                    final short band = i;
                    LinearLayout oneFreq = new LinearLayout(requireActivity().getApplicationContext());
                    oneFreq.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams lp_oneFreq = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp_oneFreq.setMargins(20, 10, 20, 10);
                    oneFreq.setLayoutParams(lp_oneFreq);
                    TextView textView1 = new TextView(requireActivity().getApplicationContext());
                    textView1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView1.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView1.setText((musicBinder.getCenterFreq(band) / 1000) + "Hz");
                    textView1.setTextSize(16);
                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    textView1.setWidth(180);
                    oneFreq.addView(textView1);
                    TextView textView2 = new TextView(requireActivity().getApplicationContext());
                    textView2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView2.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView2.setText(minEqualizer / 100 + "dB");
                    oneFreq.addView(textView2);
                    SeekBar seekBar = new SeekBar(requireActivity().getApplicationContext());
                    seekBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        seekBar.setMaxHeight(10);
                        seekBar.setMinHeight(10);
                    }
                    seekBar.setProgressDrawable(AppCompatResources.getDrawable(requireActivity().getApplicationContext(), R.drawable.seekbar_progress));
                    seekBar.setThumb(AppCompatResources.getDrawable(requireActivity().getApplicationContext(), R.drawable.pause_circle_black));
                    seekBar.setMax((int) (maxEqualizer - minEqualizer));
                    Log.i("Equalizer", "band " + String.valueOf((int) band) + " is " + String.valueOf((int) musicBinder.getBandLevel(band)));
                    seekBar.setProgress(musicBinder.getBandLevel(band) - minEqualizer);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }

                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                Log.i("Equalizer", "set band " + String.valueOf((int) band) + " to " + String.valueOf((int) progress) + "+" + String.valueOf((int) minEqualizer));
                                musicBinder.setBandLevel(band, (short) (progress + minEqualizer));
                            }
                        }
                    });
                    oneFreq.addView(seekBar);
                    seekBars.add(seekBar);
                    TextView textView3 = new TextView(requireActivity().getApplicationContext());
                    textView3.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView3.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView3.setText(maxEqualizer / 100 + "dB");
                    oneFreq.addView(textView3);
                    linearLayout.addView(oneFreq);
                }
                spinner_ps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.i(TAG, "Selected preset item " + String.valueOf(position));
                        musicBinder.usePreset((short) position);
                        for (short i = 0; i < musicBinder.getNumBands(); i++) {
                            seekBars.get((int) i).setProgress(musicBinder.getBandLevel(i) - minEqualizer);
                        }
                        Log.i(TAG, "Not first use, set preset " + String.valueOf(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                Button button1 = new Button(requireActivity().getApplicationContext());
                LinearLayout.LayoutParams lp_button1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp_button1.gravity = Gravity.CENTER_HORIZONTAL;
                button1.setLayoutParams(lp_button1);
                button1.setText(getString(R.string.reset));
                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        musicBinder.resetEqSet();
                        Log.i("Equalizer", "Number of presets is " + String.valueOf((int) musicBinder.getNumPresets()));
                        for (short i = 0; i < musicBinder.getNumBands(); i++) {
                            seekBars.get((int) i).setProgress(musicBinder.getBandLevel(i) - minEqualizer);
                        }
                    }
                });
                linearLayout.addView(button1);
                eqDialog.setView(linearLayout);
                eqDialog.create();
                eqDialog.show();
            }
        });
        NP_play.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            public void onClick(View view) {
                if (musicBinder.getCurrentMusic() != null) {
                    if (musicBinder.isPlaying()) {
                        musicBinder.Pause();
                        NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.play_black));
                        NP_play.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    } else {
                        musicBinder.Play();
                        NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.pause_black));
                        NP_play.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        musicBinder = (MusicService.MusicBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicBinder = null;
    }

    class MyTask extends TimerTask {
        @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
        @Override
        public void run() {
            if (musicBinder != null) {
                Music temp_music = musicBinder.getCurrentMusic();
                if (temp_music != null) {
                    if (prevMusic == null || prevMusic != temp_music) {
                        NP_songName.setText(temp_music.getName());
                        NP_songSinger.setText(temp_music.getSinger());
                        NP_tTime.setText(MusicUtils.formatTime(temp_music.getDuration()));
                        NP_seekbar.setMax(temp_music.getDuration());
                        prevMusic = temp_music;
                    }
                    if (isAdded()) {
                        if (musicBinder.isPlaying()) {
                            NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.pause_black));
                        } else {
                            NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.play_black));
                        }
                    }
                    NP_play.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    int currentPosition = musicBinder.getCurrentPosition();

                    NP_cTime.setText(MusicUtils.formatTime(currentPosition));
                    NP_seekbar.setProgress(currentPosition);
                }
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.i(getString(R.string.title_NowPlaying), "progress:" + progress + ",fromUser:" + fromUser);
        if (fromUser && musicBinder.getCurrentMusic() != null) {
            musicBinder.setPosition(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i(getString(R.string.title_NowPlaying), "Used onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(getString(R.string.title_NowPlaying), "Used onStopTrackingTouch");
    }
}
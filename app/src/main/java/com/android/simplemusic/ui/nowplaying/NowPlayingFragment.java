package com.android.simplemusic.ui.nowplaying;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.Music;
import com.android.simplemusic.MusicService;
import com.android.simplemusic.MusicUtils;
import com.android.simplemusic.R;
import com.android.simplemusic.databinding.FragmentNowplayingBinding;

import java.util.Timer;
import java.util.TimerTask;

public class NowPlayingFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ServiceConnection {

    private FragmentNowplayingBinding binding;
    private TextView NP_songName;
    private TextView NP_songSinger;
    private TextView NP_cTime;
    private SeekBar NP_seekbar;
    private TextView NP_tTime;
    private ImageButton NP_prev;
    private ImageButton NP_play;
    private ImageButton NP_next;
    private Timer timer;
    private Music prevMusic;
    private MusicService.MusicBinder musicBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(requireActivity().getApplicationContext(), MusicService.class);
        requireActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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
        NP_prev = root.findViewById(R.id.NP_prev);
        NP_play = root.findViewById(R.id.NP_play);
        NP_next = root.findViewById(R.id.NP_next);
        timer = new Timer();
        NP_seekbar.setOnSeekBarChangeListener(this);
        timer.schedule(new MyTask(), 0, 200);
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
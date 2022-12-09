package com.android.simplemusic.ui.nowplaying;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

import com.android.simplemusic.Data;
import com.android.simplemusic.Music;
import com.android.simplemusic.MusicUtils;
import com.android.simplemusic.R;
import com.android.simplemusic.databinding.FragmentNowplayingBinding;

import java.util.Timer;
import java.util.TimerTask;

public class NowPlayingFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private FragmentNowplayingBinding binding;
    private Data app;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NowPlayingViewModel nowPlayingViewModel =
                new ViewModelProvider(this).get(NowPlayingViewModel.class);

        binding = FragmentNowplayingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        app = (Data) requireActivity().getApplication();
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
        timer.schedule(new MyTask(), 0, 1000);
        NP_play.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            public void onClick(View view) {
                if (app.mediaPlayer.isPlaying()) {
                    app.mediaPlayer.pause();
                    NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.play_black));
                    NP_play.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } else {
                    app.mediaPlayer.start();
                    NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.pause_black));
                    NP_play.setScaleType(ImageView.ScaleType.FIT_CENTER);
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

    class MyTask extends TimerTask {
        @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
        @Override
        public void run() {
            if (app.curMusic != null && app.mediaPlayer != null) {
                if (prevMusic == null || prevMusic != app.curMusic) {
                    String songName = app.curMusic.getName();
                    String songSinger = app.curMusic.getSinger();
                    int totalTime = app.mediaPlayer.getDuration();
                    NP_songName.setText(songName);
                    NP_songSinger.setText(songSinger);
                    NP_tTime.setText(MusicUtils.formatTime(totalTime));
                    NP_seekbar.setMax(totalTime);
                    prevMusic = app.curMusic;
                    if (isAdded() && app.mediaPlayer.isPlaying()) {
                        NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.pause_black));
                    }
                    NP_play.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                int currentTime = app.mediaPlayer.getCurrentPosition();
                if (NP_seekbar.getProgress() == app.mediaPlayer.getDuration()) {
                    NP_cTime.setText("0:00");
                    NP_seekbar.setProgress(0);
                    app.mediaPlayer.seekTo(0);
                    if (isAdded() && !app.mediaPlayer.isPlaying()) {
                        NP_play.setImageDrawable(requireActivity().getDrawable(R.drawable.play_black));
                    }
                } else {
                    NP_cTime.setText(MusicUtils.formatTime(currentTime));
                    NP_seekbar.setProgress(currentTime);
                }
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.i(getString(R.string.title_NowPlaying), "progress:" + progress + ",fromUser:" + fromUser);
        if (fromUser && app.mediaPlayer != null) {
            app.mediaPlayer.seekTo(progress);
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
package com.android.simplemusic.ui.localmusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.Data;
import com.android.simplemusic.Music;
import com.android.simplemusic.MusicListAdapter;
import com.android.simplemusic.MusicUtils;
import com.android.simplemusic.R;
import com.android.simplemusic.databinding.FragmentLocalmusicBinding;

import java.io.IOException;
import java.util.ArrayList;

public class LocalMusicFragment extends Fragment {
    private static final int REQUEST_CODE = 1024;
    private static final String NOTIFICATION_MUSIC_ID = "NOTIFICATION_MUSIC_ID";
    private static final int NOTIFICATION_ID = 1;

    private FragmentLocalmusicBinding binding;
    private Data app;
    private ListView musicList;
    private ArrayList<Music> musicData;
    private MusicListAdapter musicListAdapter;
    private NotificationChannel nChannel;
    private Notification notification;
    private SearchManager searchManager;
    private SearchView searchView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocalMusicViewModel localMusicViewModel = new ViewModelProvider(this).get(LocalMusicViewModel.class);
        binding = FragmentLocalmusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 判断权限获取情况
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        // 获取全局变量
        app = (Data) requireActivity().getApplication();
        // 初始化音乐列表
        musicList = root.findViewById(R.id.musicList);
        musicData = MusicUtils.getMusicData(requireActivity());
        musicListAdapter = new MusicListAdapter(requireActivity(), R.layout.music_item, musicData);
        musicList.setAdapter(musicListAdapter);
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("RemoteViewLayout")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (app.mediaPlayer == null) {
                        app.mediaPlayer = new MediaPlayer();
                    } else {
                        app.mediaPlayer.reset();
                    }
                    Music temp_music = musicListAdapter.getItem(position);
                    app.curMusic = temp_music;
                    app.mediaPlayer.setDataSource(temp_music.getPath());
                    app.mediaPlayer.prepare();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        nChannel = new NotificationChannel(NOTIFICATION_MUSIC_ID, getString(R.string.music_channel), NotificationManager.IMPORTANCE_DEFAULT);
                        app.nManager.createNotificationChannel(nChannel);
                    }
                    int progressMax = temp_music.getDuration();
                    int progressCurrent = 0;
                    notification = new NotificationCompat.Builder(requireActivity(), NOTIFICATION_MUSIC_ID)
                            .setContentTitle(temp_music.getName())
                            .setContentText(temp_music.getSinger())
                            .setSmallIcon(R.drawable.music_note_black)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                            .setWhen(System.currentTimeMillis())
                            .build();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        app.nManager.notify(NOTIFICATION_ID, notification);
                    }
                    /*remoteViews = new RemoteViews(getPackageName(), R.layout.broadcast);
                    remoteViews.setTextViewText(R.id.bc_title, temp_music.getName());
                    if (mediaPlayer.isPlaying()) {
                        remoteViews.setImageViewResource(R.id.bc_play_pause, R.drawable.pause_black);
                    } else {
                        remoteViews.setImageViewResource(R.id.bc_play_pause, R.drawable.play_black);
                    }
                    remoteViews.setImageViewResource(R.id.bc_next, R.drawable.next_black);
                    Intent play = new Intent(getPackageName() + ".PLAY");
                    PendingIntent pi_play = PendingIntent.getBroadcast(MainActivity.this, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.bc_play_pause, pi_play);
                    notification.contentView = remoteViews;
                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, notification);*/
                    /*nManager.notify(NOTIFICATION_ID, ncBuilder.build());*/
                    app.mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = root.findViewById(R.id.musicSearch);
        searchView.setIconifiedByDefault(false);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setQueryHint(getString(R.string.queryHint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    musicListAdapter.getFilter().filter(newText);
                } else {
                    musicList.clearTextFilter();
                    musicListAdapter.getFilter().filter("");
                }
                return false;
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
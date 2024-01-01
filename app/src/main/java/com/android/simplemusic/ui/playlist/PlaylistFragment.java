package com.android.simplemusic.ui.playlist;

import static com.android.simplemusic.utils.MusicUtils.getPlaylistData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.adapter.MenuListAdapter;
import com.android.simplemusic.bean.Playlist;
import com.android.simplemusic.databinding.FragmentPlaylistBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.vm.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistFragment extends Fragment implements ServiceConnection {
    private final String TAG = "PlaylistFragment";

    private ArrayList<Playlist> playlists;

    private FragmentPlaylistBinding binding;
    private MainViewModel model;
    private MusicService musicService;
    private PlaylistDBHelper mDBHelper;
    private MenuListAdapter menuListAdapter;
    private PlaylistReceiver pr;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 绑定Music Service
        Intent intent = new Intent(requireActivity().getApplicationContext(), MusicService.class);
        requireActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        // 注册接收是否获取权限广播的广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Definition.PERMISSION_ACQUIRED);
        intentFilter.addAction(Definition.DBH_ACQUIRED);
        pr = new PlaylistReceiver();
        requireContext().registerReceiver(pr, intentFilter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // 绑定ViewModel
        model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mDBHelper = model.getmDBHelper().getValue();
        // PlaylistViewModel playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 初始化播放列表列表
        List<Playlist> playlists;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            playlists = getPlaylistData(mDBHelper);
        } else {
            playlists = new ArrayList<Playlist>();
        }
        model.setmPlaylists((ArrayList<Playlist>) playlists);
        // 初始化播放列表列表适配器

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "onServiceConnected");
        musicService = ((MusicService.MusicBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "onServiceDisconnected");
        musicService = null;
    }

    // 广播接收器
    public class PlaylistReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received message: " + intent.getAction());
            if (Objects.equals(intent.getAction(), Definition.DBH_ACQUIRED)) {
                mDBHelper = model.getmDBHelper().getValue();
            }
        }
    }

    //
}
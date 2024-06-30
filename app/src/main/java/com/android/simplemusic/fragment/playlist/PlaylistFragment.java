package com.android.simplemusic.fragment.playlist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.R;
import com.android.simplemusic.adapter.PlaylistListAdapter;
import com.android.simplemusic.application.MainApplication;
import com.android.simplemusic.room.dao.PlaylistDao;
import com.android.simplemusic.databinding.FragmentPlaylistBinding;
import com.android.simplemusic.room.entity.Playlist;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistFragment extends Fragment implements ServiceConnection {
    private final String TAG = PlaylistFragment.class.getSimpleName();

    private FragmentPlaylistBinding binding;
    private ListView mListView;
    private PlaylistViewModel model;
    private MusicService musicService;
    private PlaylistListAdapter mListAdapter;
    private PlaylistDao playlistDao;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 绑定Music Service
        Intent intent = new Intent(requireActivity().getApplicationContext(), MusicService.class);
        requireActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        // 初始化PlaylistDao
        playlistDao = MainApplication.getInstance().getPlaylistDatabase().playlistDao();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // 绑定ViewModel
        model = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 初始化播放列表列表适配器
        mListAdapter = new PlaylistListAdapter(requireActivity(), R.layout.playlist_item, new ArrayList<Playlist>());
        mListView = root.findViewById(R.id.playlistList);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "clicked " + Objects.requireNonNull(mListAdapter.getItem(position)).getName());
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        List<Playlist> playlists;
        if (PermissionUtils.checkStoragePermission(requireActivity())) {
            playlists = playlistDao.getAllPlaylists();
        } else {
            playlists = new ArrayList<Playlist>();
        }
        model.setPlaylists((ArrayList<Playlist>) playlists);
        if (mListAdapter != null) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListAdapter.setPlaylistList(playlists);
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
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
}
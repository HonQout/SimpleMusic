package com.android.simplemusic.ui.localmusic;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.Music;
import com.android.simplemusic.MusicListAdapter;
import com.android.simplemusic.MusicService;
import com.android.simplemusic.MusicUtils;
import com.android.simplemusic.R;
import com.android.simplemusic.databinding.FragmentLocalmusicBinding;

import java.util.ArrayList;

public class LocalMusicFragment extends Fragment implements ServiceConnection {
    private static final int REQUEST_CODE = 1024;
    private static final String NOTIFICATION_MUSIC_ID = "NOTIFICATION_MUSIC_ID";
    private static final String TAG = "LocalMusicFragment";
    private static final int NOTIFICATION_ID = 1;

    private FragmentLocalmusicBinding binding;
    private ListView musicList;
    private ArrayList<Music> musicData;
    private MusicListAdapter musicListAdapter;
    private SearchManager searchManager;
    private SearchView searchView;
    private MusicService.MusicBinder musicBinder;
    private IntentFilter intentFilter;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(requireActivity().getApplicationContext(), MusicService.class);
        requireActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.simplemusic.PERMISSION_ACQUIRED");
        myBroadcastReceiver = new MyBroadcastReceiver();
        requireContext().registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        LocalMusicViewModel localMusicViewModel = new ViewModelProvider(this).get(LocalMusicViewModel.class);
        binding = FragmentLocalmusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 初始化音乐列表
        musicList = root.findViewById(R.id.musicList);
        musicData = MusicUtils.getMusicData(requireActivity());
        musicListAdapter = new MusicListAdapter(requireActivity(), R.layout.music_item, musicData);
        musicList.setAdapter(musicListAdapter);
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("RemoteViewLayout")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicBinder.Init(musicListAdapter.getItem(position));
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
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);
        musicList.requestFocus();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (musicListAdapter != null) {
            musicListAdapter.notifyDataSetChanged();
        }
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

    // 广播接收器
    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received message");
            if (musicListAdapter != null) {
                musicData = MusicUtils.getMusicData(requireActivity());
                musicListAdapter = new MusicListAdapter(requireActivity(), R.layout.music_item, musicData);
                musicList.setAdapter(musicListAdapter);
                musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("RemoteViewLayout")
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        musicBinder.Init(musicListAdapter.getItem(position));
                    }
                });
            }
        }
    }
}
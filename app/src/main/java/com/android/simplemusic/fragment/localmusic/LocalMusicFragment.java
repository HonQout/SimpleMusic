package com.android.simplemusic.fragment.localmusic;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.simplemusic.R;
import com.android.simplemusic.adapter.MusicRecyclerAdapter;
import com.android.simplemusic.application.MainApplication;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.room.dao.PlaylistDao;
import com.android.simplemusic.databinding.FragmentLocalMusicBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.utils.MusicUtils;
import com.android.simplemusic.view.dialog.MusicInfoDialog;
import com.android.simplemusic.vm.MainViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicFragment extends Fragment implements ServiceConnection {
    private static final String TAG = LocalMusicFragment.class.getSimpleName();

    private FragmentLocalMusicBinding binding;
    private MusicRecyclerAdapter musicRecyclerAdapter;
    private MainViewModel model;
    private MusicService musicService;
    private PlaylistDao playlistDao;
    private SharedPreferences sharedPreferences;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 绑定Music Service
        Intent intent = new Intent(requireActivity().getApplicationContext(), MusicService.class);
        requireActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        // 初始化PlaylistDao
        playlistDao = MainApplication.getInstance().getPlaylistDatabase().playlistDao();
        // 获取共享偏好
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        // 绑定ViewModel
        model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // 创建MusicRecyclerAdapter
        musicRecyclerAdapter = new MusicRecyclerAdapter(requireContext(), new ArrayList<Music>());
        musicRecyclerAdapter.setItemClickListener(new MusicRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Music music = musicRecyclerAdapter.getItem(position);
                if (music != null) {
                    model.setIndex(position);
                    model.setImage(MusicUtils.getAlbumImage(music.getPath()));
                    musicService.Init(music);
                    // TODO:add music to db
                }
            }

            @Override
            public void onItemLongClick(int position) {
                Music musicItem = musicRecyclerAdapter.getItem(position);
                if (musicItem != null) {
                    new MusicInfoDialog(requireContext(), musicItem).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // 初始化binding
        binding = FragmentLocalMusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 初始化SearchView
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        binding.svMusic.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        binding.svMusic.setQueryHint(getString(R.string.hint_searchview_music));
        binding.svMusic.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    musicRecyclerAdapter.getFilter().filter(newText);
                } else {
                    musicRecyclerAdapter.getFilter().filter("");
                }
                return false;
            }
        });
        // 初始化MusicRecyclerView
        LinearLayoutManager manager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        binding.rvMusic.setLayoutManager(manager);
        binding.rvMusic.setAdapter(musicRecyclerAdapter);
        return root;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (musicRecyclerAdapter != null) {
            Log.i(TAG, String.valueOf(musicRecyclerAdapter.getItemCount()));
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
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String message = messageEvent.getMessage();
        if (message != null) {
            Log.i(TAG, "Received MessageEvent, message is " + message);
            if (message.equals(Definition.UPDATE_MUSIC_LIST)) {
                if (model.getMusicList().getValue() != null) {
                    setMusicList(model.getMusicList().getValue());
                }
            }
        }
    }

    private void setMusicList(List<Music> musicList) {
        int scrollPosition = 0;
        RecyclerView.LayoutManager layoutManager = binding.rvMusic.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            scrollPosition = linearLayoutManager.findLastVisibleItemPosition();
        }
        musicRecyclerAdapter.setMusicList(musicList);
        layoutManager = binding.rvMusic.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPosition(scrollPosition);
        }
    }
}
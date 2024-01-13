package com.android.simplemusic.fragment.localmusic;

import static com.android.simplemusic.utils.FileUtils.openDir;
import static com.android.simplemusic.utils.MusicUtils.formatSize;
import static com.android.simplemusic.utils.MusicUtils.formatTime;
import static com.android.simplemusic.utils.MusicUtils.getMusicData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.R;
import com.android.simplemusic.adapter.MusicListAdapter;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.databinding.FragmentLocalmusicBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.vm.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LocalMusicFragment extends Fragment implements ServiceConnection {
    private static final String TAG = "LocalMusicFragment";

    private FragmentLocalmusicBinding binding;
    private ListView musicList;
    private List<Music> musicData;
    private MusicListAdapter musicListAdapter;
    private SearchManager searchManager;
    private SearchView searchView;
    private MainViewModel model;
    private MusicService musicService;
    private LocalMusicBR localMusicBR;
    private PlaylistDBHelper mDBHelper;

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
        localMusicBR = new LocalMusicBR();
        requireContext().registerReceiver(localMusicBR, intentFilter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // 绑定ViewModel
        model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // LocalMusicViewModel localMusicViewModel = new ViewModelProvider(this).get(LocalMusicViewModel.class);
        binding = FragmentLocalmusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 初始化音乐列表适配器
        musicListAdapter = new MusicListAdapter(requireActivity(), R.layout.music_item, new ArrayList<Music>());
        musicList = root.findViewById(R.id.musicList);
        musicList.setAdapter(musicListAdapter);
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicService.Init(Objects.requireNonNull(musicListAdapter.getItem(position)));
                if (mDBHelper != null) {
                    mDBHelper.insert("RecentPlay",
                            new ArrayList<Music>(Collections.singletonList(musicListAdapter.getItem(position))));
                }
            }
        });
        musicList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Music musicItem = musicListAdapter.getItem(position);
                @SuppressLint("InflateParams") View view1 = getLayoutInflater().inflate(R.layout.music_info_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(R.string.music_info);
                builder.setIcon(R.mipmap.ic_launcher);
                if (musicItem != null) {
                    TextView mid_song = view1.findViewById(R.id.mid_name);
                    TextView mid_singer = view1.findViewById(R.id.mid_artist);
                    TextView mid_duration = view1.findViewById(R.id.mid_duration);
                    TextView mid_path = view1.findViewById(R.id.mid_path);
                    TextView mid_size = view1.findViewById(R.id.mid_size);
                    Button mid_play = view1.findViewById(R.id.mid_play);
                    Button mid_vifm = view1.findViewById(R.id.mid_vifm);
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mid_song.setText(String.format(getString(R.string.mid_song), musicItem.getTitle()));
                            mid_singer.setText(String.format(getString(R.string.mid_singer), musicItem.getArtist()));
                            mid_duration.setText(String.format(getString(R.string.mid_duration), formatTime(musicItem.getDuration())));
                            mid_path.setText(String.format(getString(R.string.mid_path), musicItem.getPath()));
                            mid_size.setText(String.format(getString(R.string.mid_size), formatSize(musicItem.getSize())));
                            mid_play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    musicService.Init(Objects.requireNonNull(musicListAdapter.getItem(position)));
                                    if (mDBHelper != null) {
                                        mDBHelper.insert("RecentPlay",
                                                new ArrayList<Music>(Collections.singletonList(musicListAdapter.getItem(position))));
                                    }
                                }
                            });
                            mid_vifm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDir(requireActivity(), musicItem.getPath());
                                }
                            });
                        }
                    });
                }
                builder.setView(view1);
                builder.create().show();
                return true;
            }
        });
        // 初始化顶部菜单栏
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
        Log.i(TAG, "onResume");
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Got music data in onResume");
            musicData = getMusicData(requireActivity());
        } else {
            Log.i(TAG, "Hadn't get music data in onResume");
            musicData = new ArrayList<Music>();
        }
        model.setmMusicList(musicData);
        if (musicListAdapter != null) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    musicListAdapter.setMusicList(musicData);
                    musicListAdapter.notifyDataSetChanged();
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

    // 广播接收器
    public class LocalMusicBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received message: " + intent.getAction());
            if (Objects.equals(intent.getAction(), Definition.PERMISSION_ACQUIRED)) {
                musicData = model.getmMusicList().getValue();
                if (musicData != null) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicListAdapter.setMusicList(musicData);
                            musicListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } else if (Objects.equals(intent.getAction(), Definition.DBH_ACQUIRED)) {
                mDBHelper = model.getmDBHelper().getValue();
            }
        }
    }
}
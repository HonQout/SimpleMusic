package com.android.simplemusic.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.databinding.ActivityMainBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.utils.ColorUtils;
import com.android.simplemusic.utils.MusicUtils;
import com.android.simplemusic.utils.PermissionUtils;
import com.android.simplemusic.utils.ThemeUtils;
import com.android.simplemusic.vm.MainViewModel;
import com.google.android.material.navigation.NavigationBarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1024;
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private ThemeUtils themeUtils;
    private MenuItem menu_settings;
    private NavHostFragment navHostFragment;
    private SharedPreferences sharedPreferences;
    private NavController navController;
    private MusicService.MusicBinder musicBinder;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
            Log.i(TAG, "Connected to Music Service");
            EventBus.getDefault().post(new MessageEvent(Definition.SERVICE_CONNECTED, "MainActivity"));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBinder = null;
            Log.i(TAG, "Disconnected to Music Service");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 初始化界面
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        menu_settings = (MenuItem) findViewById(R.id.menu_settings);
        setSupportActionBar(binding.toolbarMain);
        // 初始化NavView
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_LocalMusic, R.id.navigation_Playlist)
                .build();
        // 初始化NavHostFragment
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.isChecked()) {
                    return true;
                } else {
                    return NavigationUI.onNavDestinationSelected(item, navController);
                }
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        // 初始化DockBar
        binding.dockBar.setImageViewOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                startActivity(intent);
            }
        });
        binding.dockBar.setImageButton1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.PREV));
            }
        });
        binding.dockBar.setImageButton2OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.PLAY_PAUSE));
            }
        });
        binding.dockBar.setImageButton3OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.NEXT));
            }
        });
        themeUtils = new ThemeUtils(this) {
            @Override
            public void whenEnabledNightMode() {
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
                binding.getRoot().setBackgroundColor(Color.BLACK);
                binding.bottomNavigationView.setBackgroundColor(Color.BLACK);
            }

            @Override
            public void whenDisabledNightMode() {
                int color = ColorUtils.analyzeColor(MainActivity.this,
                        sharedPreferences.getString("theme_color", "white"));
                if (sharedPreferences.getBoolean("immersion_status_bar", true)) {
                    getWindow().setStatusBarColor(color);
                } else {
                    getWindow().setStatusBarColor(Color.GRAY);
                }
                if (sharedPreferences.getBoolean("immersion_navigation_bar", true)) {
                    getWindow().setNavigationBarColor(color);
                } else {
                    getWindow().setNavigationBarColor(Color.TRANSPARENT);
                }
                binding.toolbarMain.setBackgroundColor(color);
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbarMain.setTitleTextColor(Color.BLACK);
                    if (menu_settings != null) {
                        menu_settings.setIcon(R.drawable.settings_black);
                    }
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbarMain.setTitleTextColor(Color.WHITE);
                    if (menu_settings != null) {
                        menu_settings.setIcon(R.drawable.settings_white);
                    }
                }
            }
        };
        // 绑定音乐服务
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "Bound to Music Service");
        // 绑定ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getMusicList().observe(this, new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                if (musicList != null) {
                    Log.i(TAG, "MusicList in viewmodel changed, prepare to set music list. New size is: "
                            + musicList.size());
                    EventBus.getDefault().post(new MessageEvent(Definition.UPDATE_MUSIC_LIST));
                }
            }
        });
        viewModel.getImage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                if (bitmap != null) {
                    binding.dockBar.setImageViewBitmap(bitmap);
                } else {
                    binding.dockBar.setImageViewDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.record, null));
                }
            }
        });
        if (viewModel.getImage().getValue() != null) {
            binding.dockBar.setImageViewBitmap(viewModel.getImage().getValue());
        } else {
            binding.dockBar.setImageViewDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.record, null));
        }
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
        if (PermissionUtils.checkStoragePermission(this)) {
            if (viewModel.getMusicList().getValue() == null || sharedPreferences.getBoolean("strong_scan_mode", false)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int minDuration = sharedPreferences.getInt("filter_short_audio", 0);
                        int minSize = sharedPreferences.getInt("filter_small_audio", 0);
                        List<Music> musicList = MusicUtils.getMusicData(MainActivity.this, minDuration, minSize);
                        Log.i(TAG, "Got music data, size is: " + musicList.size());
                        viewModel.setMusicList(musicList);
                    }
                }).start();
            }
        } else { //请求权限
            PermissionUtils.requestStoragePermission(this, REQUEST_CODE);
        }
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

    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    // 获取权限的结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (!PermissionUtils.checkStoragePermission(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage(getString(R.string.require_storage_permission));
                builder.setPositiveButton(getString(R.string.authorize), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Toast.makeText(MainActivity.this, getString(R.string.grant_permission_hint_13), Toast.LENGTH_SHORT).show();
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Toast.makeText(MainActivity.this, getString(R.string.grant_permission_hint_10), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.grant_permission_hint_6), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNeutralButton(getString(R.string.check_again), null);
                builder.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Exited because of not getting required permissions.");
                        finish();
                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PermissionUtils.checkStoragePermission(MainActivity.this)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            } else {
                EventBus.getDefault().post(new MessageEvent(Definition.PERMISSION_ACQUIRED));
            }
        }
    }

    // 创建菜单事件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu_settings = menu.findItem(R.id.menu_settings);
        EventBus.getDefault().post(new MessageEvent(Definition.THEME));
        return super.onCreateOptionsMenu(menu);
    }

    // 菜单响应事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent_settings = new Intent(MainActivity.this, Settings.class);
            startActivity(intent_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    // 处理EventBus事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String message = messageEvent.getMessage();
        switch (message) {
            case Definition.SERVICE_CONNECTED:
                Log.i(TAG, "Received Message SERVICE_CONNECTED");
                if (messageEvent.getContent().equals("MainActivity")) {
                    Music currentMusic = musicBinder.getService().getCurrentMusic();
                    if (currentMusic != null) {
                        binding.dockBar.setTextView1Text(currentMusic.getTitle());
                        binding.dockBar.setTextView2Text(currentMusic.getArtist());
                    }
                }
                if (musicBinder.getService().isPlaying()) {
                    binding.dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_black, null));
                } else {
                    binding.dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_black, null));
                }
                break;
            case Definition.INIT:
                Log.i(TAG, "Received Message INIT");
                binding.dockBar.setTextView1Text(musicBinder.getService().getCurrentMusic().getTitle());
                binding.dockBar.setTextView2Text(musicBinder.getService().getCurrentMusic().getArtist());
                break;
            case Definition.PLAY:
                Log.i(TAG, "Received Message PLAY");
                binding.dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_black, null));
                break;
            case Definition.PAUSE:
                Log.i(TAG, "Received Message PAUSE");
                binding.dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_black, null));
                break;
            case Definition.COMPLETION:
                Log.i(TAG, "Received Message COMPLETION");
                if (!musicBinder.getService().isPlaying()) {
                    binding.dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_black, null));
                }
                break;
            case Definition.PREV:
                Log.i(TAG, "Received Message PREV");
                if (viewModel.getIndex().getValue() != null && viewModel.getMusicList().getValue() != null) {
                    int index = viewModel.getIndex().getValue();
                    index = index - 1 >= 0 ? index - 1 : viewModel.getMusicList().getValue().size() - 1;
                    Music music = viewModel.getMusicList().getValue().get(index);
                    musicBinder.getService().Init(music);
                    viewModel.setIndex(index);
                } else {
                    Log.i(TAG, "No music set.");
                }
                break;
            case Definition.NEXT:
                Log.i(TAG, "Received Message PREV");
                if (viewModel.getIndex().getValue() != null && viewModel.getMusicList().getValue() != null) {
                    int index = viewModel.getIndex().getValue();
                    index = index + 1 < viewModel.getMusicList().getValue().size() ? index + 1 : 0;
                    Music music = viewModel.getMusicList().getValue().get(index);
                    musicBinder.getService().Init(music);
                    viewModel.setIndex(index);
                } else {
                    Log.i(TAG, "No music set.");
                }
                break;
            case Definition.THEME:
                Log.i(TAG, "Received Message THEME_COLOR");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        themeUtils.onThemeSettingChanged(MainActivity.this);
                    }
                });
                break;
            default:
                break;
        }
    }
}
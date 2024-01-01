package com.android.simplemusic.activity;

import static com.android.simplemusic.utils.MusicUtils.getMusicData;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.databinding.ActivityMainBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.service.MusicService;
import com.android.simplemusic.view.DockBar;
import com.android.simplemusic.vm.MainViewModel;
import com.google.android.material.navigation.NavigationBarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int UiMode;
    private static final int REQUEST_CODE = 1024;
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private MainViewModel model;
    private MenuItem menu_settings;
    private NavHostFragment navHostFragment;
    private DockBar dockBar;
    private NavController navController;
    private MusicService.MusicBinder musicBinder;
    private PlaylistDBHelper mDBHelper;
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
        // 获取绑定
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 设置系统UI可见性
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // 初始化Toolbar
        binding.toolbarMain.setTitle("");
        setSupportActionBar(binding.toolbarMain);
        // 初始化菜单-设置
        menu_settings = (MenuItem) findViewById(R.id.menu_settings);
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
        dockBar = findViewById(R.id.dockBar);
        dockBar.setImageViewOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                startActivity(intent);
            }
        });
        dockBar.setImageButton1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.PREV));
            }
        });
        dockBar.setImageButton2OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.PLAY_PAUSE));
            }
        });
        dockBar.setImageButton3OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent(Definition.NEXT));
            }
        });
        // 绑定音乐服务
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "Bound to Music Service");
        // 绑定ViewModel
        model = new ViewModelProvider(this).get(MainViewModel.class);
        // 请求权限
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
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
        mDBHelper = PlaylistDBHelper.getInstance(this, 1);
        model.setmDBHelper(mDBHelper);
        mDBHelper.openWriteLink();
        Intent intent = new Intent(Definition.DBH_ACQUIRED);
        sendBroadcast(intent);
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
        // 请求权限
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        mDBHelper.closeLink();
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setIcon(R.mipmap.ic_launcher);
                ad.setTitle(getString(R.string.app_name));
                ad.setMessage(getString(R.string.permissionHint));
                ad.setPositiveButton(getString(R.string.authorize), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Toast.makeText(getApplicationContext(), getString(R.string.hint_permission_10), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.hint_permission_9), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ad.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                ad.setCancelable(false);
                ad.create();
                ad.show();
            } else {
                Intent intent_pa = new Intent(Definition.PERMISSION_ACQUIRED);
                intent_pa.setPackage(getPackageName());
                List<Music> musicList = getMusicData(this);
                model.setmMusicList(musicList);
                Log.i(TAG, "Permission acquired and data acquired");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent_pa);
            }
        }
    }

    // 创建菜单事件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu_settings = menu.findItem(R.id.menu_settings);
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            menu_settings.setIcon(R.drawable.settings_black);
        } else {
            menu_settings.setIcon(R.drawable.settings_white);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // 菜单响应事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    // 处理EventBus事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String eventMessage = messageEvent.getMessage();
        switch (eventMessage) {
            case Definition.SERVICE_CONNECTED:
                Log.i(TAG, "Received Message SERVICE_CONNECTED");
                if (messageEvent.getContent().equals("MainActivity")) {
                    dockBar.setTextView1Text(musicBinder.getService().getCurrentMusic().getTitle());
                    dockBar.setTextView2Text(musicBinder.getService().getCurrentMusic().getArtist());
                }
                if (musicBinder.getService().isPlaying()) {
                    dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_black, null));
                } else {
                    dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_black, null));
                }
                break;
            case Definition.INIT:
                Log.i(TAG, "Received Message INIT");
                dockBar.setTextView1Text(musicBinder.getService().getCurrentMusic().getTitle());
                dockBar.setTextView2Text(musicBinder.getService().getCurrentMusic().getArtist());
                break;
            case Definition.PLAY:
                Log.i(TAG, "Received Message PLAY");
                dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_black, null));
                break;
            case Definition.PAUSE:
                Log.i(TAG, "Received Message PAUSE");
                dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_black, null));
                break;
            case Definition.COMPLETION:
                Log.i(TAG, "Received Message COMPLETION");
                if (!musicBinder.getService().isPlaying()) {
                    dockBar.setImageButton2Drawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_black, null));
                }
                break;
            default:
                break;
        }
    }
}
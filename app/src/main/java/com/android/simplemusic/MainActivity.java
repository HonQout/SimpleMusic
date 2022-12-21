package com.android.simplemusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.simplemusic.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private int UiMode;
    private static final int NOTIFICATION_ID = 1024;
    private static final int REQUEST_CODE = 1024;
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private Toolbar toolbar_main;
    private MenuItem menu_settings;
    private BottomNavigationView nav_view;
    private NavHostFragment navHostFragment;
    private NavController navController;
    private MusicService.MusicBinder musicBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
            Log.i(TAG, "Connected to Music Service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取绑定
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 设置系统UI可见性
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // 初始化Toolbar
        toolbar_main = (Toolbar) findViewById(R.id.toolbar1);
        toolbar_main.setTitle("");
        setSupportActionBar(toolbar_main);
        // 初始化菜单-设置
        menu_settings = (MenuItem) findViewById(R.id.menu_settings);
        // 初始化NavView
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_LocalMusic, R.id.navigation_NowPlaying, R.id.navigation_Playlist)
                .build();
        // 初始化NavHostFragment
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        navController.getNavigatorProvider().addNavigator(new CustomNavigator(this, getSupportFragmentManager(), R.id.navHostFragment));
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "Bound to Music Service");
        // 判断权限获取情况
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
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
                        Toast.makeText(getApplicationContext(), getString(R.string.guaranteeHint), Toast.LENGTH_SHORT).show();
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
                Intent intent_pa = new Intent("com.android.simplemusic.PERMISSION_ACQUIRED");
                intent_pa.setPackage(getPackageName());
                Log.i(TAG, "sent message to remind musicList in LocalMusic Fragment to update data");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent_pa);
            }
        }
    }

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

    // 定义菜单响应事件
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent_settings = new Intent(MainActivity.this, Settings.class);
                startActivity(intent_settings);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 定义接受Intent时启动的Fragment
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int position;
        if (intent != null) {
            position = intent.getIntExtra("position", 0);
            // TODO: 切换Fragment为正在播放
        }
    }

    // 定义保存状态的方法
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    // 定义销毁活动的动作
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }
}
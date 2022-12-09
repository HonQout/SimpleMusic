package com.android.simplemusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.simplemusic.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private int UiMode;
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 1024;
    private Data app;

    private ActivityMainBinding binding;
    private Toolbar toolbar1;
    private MenuItem menu_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
        toolbar1.setTitle("");
        setSupportActionBar(toolbar1);
        menu_settings = (MenuItem) findViewById(R.id.menu_settings);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_LocalMusic, R.id.navigation_NowPlaying, R.id.navigation_Playlist)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        app = (Data) getApplication();
        if (app.mediaPlayer == null) {
            app.mediaPlayer = new MediaPlayer();
        }
        if (app.nManager == null) {
            app.nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
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

    // 定义销毁活动的动作
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (app.mediaPlayer != null) {
            app.mediaPlayer.stop();
            app.mediaPlayer.release();
        }
        if (app.nManager != null) {
            app.nManager.cancel(NOTIFICATION_ID);
        }
    }
}
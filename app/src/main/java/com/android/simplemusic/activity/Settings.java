package com.android.simplemusic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.android.simplemusic.R;
import com.android.simplemusic.databinding.ActivitySettingsBinding;
import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;
import com.android.simplemusic.utils.ColorUtils;
import com.android.simplemusic.utils.ThemeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Settings extends AppCompatActivity {
    private static final String TAG = Settings.class.getSimpleName();
    private ActivitySettingsBinding binding;
    private ThemeUtils themeUtils;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 初始化界面
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSettings);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        themeUtils = new ThemeUtils(this) {
            @Override
            public void whenEnabledNightMode() {
                getWindow().setStatusBarColor(Color.BLACK);
                binding.toolbarSettings.setTitleTextColor(Color.WHITE);
                binding.toolbarSettings.setBackgroundColor(Color.BLACK);
                binding.toolbarSettings.setNavigationIcon(R.drawable.arrow_back_white);
            }

            @Override
            public void whenDisabledNightMode() {
                int color = ColorUtils.analyzeColor(Settings.this,
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
                binding.toolbarSettings.setBackgroundColor(color);
                View decorView = getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbarSettings.setTitleTextColor(Color.BLACK);
                    binding.toolbarSettings.setNavigationIcon(R.drawable.arrow_back_black);
                } else {
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    binding.toolbarSettings.setTitleTextColor(Color.WHITE);
                    binding.toolbarSettings.setNavigationIcon(R.drawable.arrow_back_white);
                }
            }
        };
        binding.toolbarSettings.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        // 注册共享偏好改变监听器
        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(TAG, "Received shared preference change");
                switch (key) {
                    case "theme_color":
                        String color = sharedPreferences.getString("theme_color", "white");
                        Log.i(TAG, "THEME_COLOR changed to" + color);
                        EventBus.getDefault().post(new MessageEvent(Definition.THEME));
                        break;
                    case "night_mode":
                        String nightMode = sharedPreferences.getString("night_mode", "follow_system");
                        Log.i(TAG, "NIGHT_MODE changed to" + nightMode);
                        EventBus.getDefault().post(new MessageEvent(Definition.THEME));
                        break;
                    case "immersion_status_bar":
                        String isb = sharedPreferences.getBoolean("immersion_status_bar", true) ? "On" : "Off";
                        Log.i(TAG, "IMMERSION_STATUS_BAR changed to " + isb);
                        EventBus.getDefault().post(new MessageEvent(Definition.THEME));
                        break;
                    case "immersion_navigation_bar":
                        String inb = sharedPreferences.getBoolean("immersion_navigation_bar", true) ? "On" : "Off";
                        Log.i(TAG, "IMMERSION_NAVIGATION_BAR changed to " + inb);
                        EventBus.getDefault().post(new MessageEvent(Definition.THEME));
                        break;
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        // 显示设置碎片
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
        // 取消绑定监听器
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String eventMessage = messageEvent.getMessage();
        switch (eventMessage) {
            case Definition.THEME:
                Log.i(TAG, "Received Message THEME");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        themeUtils.onThemeSettingChanged(Settings.this);
                    }
                });
                break;
            default:
                break;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            if (preference.hasKey()) {
                // 系统声音设置
                if (preference.getKey().equals("system_sound_settings")) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}
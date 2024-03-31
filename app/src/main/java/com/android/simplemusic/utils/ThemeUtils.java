package com.android.simplemusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public abstract class ThemeUtils {
    public int uiMode;
    public String nightMode;
    public boolean isNightModeEnabled;

    public ThemeUtils(@NonNull Context context) {
        initThemeSetting(context);
        refreshUI(isNightModeEnabled);
    }

    private void initThemeSetting(@NonNull Context context) {
        uiMode = context.getApplicationContext().getResources().getConfiguration().uiMode;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        nightMode = sharedPreferences.getString("night_mode", "follow_system");
        isNightModeEnabled = nightMode.equals("on") ||
                (nightMode.equals("follow_system") && ((uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES));
    }

    private void refreshUI(boolean isNightModeEnabled) {
        if (isNightModeEnabled) {
            whenEnabledNightMode();
        } else {
            whenDisabledNightMode();
        }
    }

    public void onThemeSettingChanged(@NonNull Context context){
        initThemeSetting(context);
        refreshUI(isNightModeEnabled);
    }

    public abstract void whenEnabledNightMode();

    public abstract void whenDisabledNightMode();
}

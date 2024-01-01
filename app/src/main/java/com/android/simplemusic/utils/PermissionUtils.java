package com.android.simplemusic.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PermissionUtils {
    // 检查是否获取了单个权限
    public static boolean checkPermission(Activity activity, String permission, int requestCode) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }
}

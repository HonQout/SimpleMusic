package com.android.simplemusic.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class PermissionUtils {
    public static final String TAG = PermissionUtils.class.getSimpleName();

    // 检查权限
    public static boolean checkSelfPermission(@NonNull Activity activity, String permission) {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    // 检查权限组
    public static boolean checkSelfPermissions(@NonNull Activity activity, String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (!checkSelfPermission(activity, permission)) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    public static boolean checkStoragePermission(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO});
        } else {
            return checkSelfPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    public static boolean checkRecordAudioPermission(@NonNull Activity activity) {
        return checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
    }

    // 请求权限
    public static void requestStoragePermission(Activity activity, int REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    public static void requestRecordAudioPermission(Activity activity, int REQUEST_CODE) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
    }
}

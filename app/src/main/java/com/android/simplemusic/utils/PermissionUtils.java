package com.android.simplemusic.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class PermissionUtils {
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static final String[] storagePermission_13 = {Manifest.permission.READ_MEDIA_AUDIO};
    public static final String[] storagePermission_6 = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // 检查权限
    public static boolean checkSelfPermission(Activity activity, String permission) {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkSelfPermissions(Activity activity, String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (!checkSelfPermission(activity, permission)) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    public static boolean checkStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO});
        } else {
            return checkSelfPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    public static boolean checkRecordAudioPermission(Activity activity) {
        return checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
    }

    public static boolean checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.RECORD_AUDIO});
        } else {
            return checkSelfPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO});
        }
    }

    // 请求权限
    public static void requestStoragePermission(Activity activity, int REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    public static void requestRecordAudioPermission(Activity activity, int REQUEST_CODE) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
    }


    public static void requestPermission(Activity activity, int REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE);
        }
    }
}

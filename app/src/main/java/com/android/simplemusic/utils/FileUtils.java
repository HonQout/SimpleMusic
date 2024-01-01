package com.android.simplemusic.utils;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.core.content.FileProvider;

import java.io.File;

public class FileUtils {
    public static boolean openDir(Activity activity, String path) {
        if (activity == null) {
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent();
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                uri = FileProvider.getUriForFile(activity, "com.android.simplemusic.fileProvider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "file/*");
            try {
                startActivity(activity, intent, null);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static void openAndroidDataDirectory(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}

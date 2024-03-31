package com.android.simplemusic.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.simplemusic.definition.Definition;

public class MusicControlReceiver extends BroadcastReceiver {
    public static final String TAG = "MusicControlReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Definition.PREV:
                    Log.i(TAG, "Received PREV");
                    break;
                case Definition.PLAY:
                    Log.i(TAG, "Received PLAY");
                    break;
                case Definition.NEXT:
                    Log.i(TAG, "Received NEXT");
                    break;
            }
        }
    }
}

package com.android.simplemusic.intent;

import android.content.Intent;

import com.android.simplemusic.definition.Definition;

public class MusicIntent {
    public static final Intent intentPrev = new Intent(Definition.PREV);
    public static final Intent intentPlayPause = new Intent(Definition.PLAY_PAUSE);
    public static final Intent intentNext = new Intent(Definition.NEXT);
    public static final Intent intentStop = new Intent(Definition.STOP);
}

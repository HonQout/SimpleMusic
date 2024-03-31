package com.android.simplemusic.bean;

import android.media.audiofx.Equalizer;
import android.util.Log;

import java.util.ArrayList;

public class MyEqualizer extends Equalizer {
    private static final String TAG = MyEqualizer.class.getSimpleName();
    private static short preset = -1;
    private static boolean personalized = false;
    private static ArrayList<Short> settings = new ArrayList<Short>();

    public MyEqualizer(int priority, int audioSession) throws RuntimeException {
        super(priority, audioSession);
    }

    public void init() {
        if (preset == -1) {
            preset = (short) 0;
        }
        usePreset(preset);
        Log.i(TAG, "Use saved preset " + getPresetName(preset) + " to initialize equalizer");
        if (settings.size() == getNumberOfBands()) {
            for (short i = 0; i < getNumberOfBands(); i++) {
                setBandLevel(i, settings.get((int) i));
                Log.i(TAG, "Set band " + i + " to " + settings.get((int) i));
            }
            Log.i(TAG, "Equalizer initialization finished");
        }
    }

    @Override
    public void setBandLevel(short band, short level) throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        super.setBandLevel(band, level);
        personalized = true;
        settings = new ArrayList<Short>();
        for (short i = 0; i < getNumberOfBands(); i++) {
            settings.add(getBandLevel(i));
        }
    }

    @Override
    public void usePreset(short preset) throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        super.usePreset(preset);
        this.preset = preset;
        Log.i(TAG, "Use preset " + preset);
    }

    public void reset() {
        if (preset == -1) {
            preset = (short) 0;
        }
        usePreset(preset);
    }
}

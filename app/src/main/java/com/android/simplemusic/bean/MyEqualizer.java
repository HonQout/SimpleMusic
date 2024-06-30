package com.android.simplemusic.bean;

import android.media.audiofx.Equalizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyEqualizer extends Equalizer {
    private static final String TAG = MyEqualizer.class.getSimpleName();
    private short preset = -1;
    private boolean personalized = false;
    private ArrayList<Short> settings = new ArrayList<Short>();

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
    public void setBandLevel(short band, short level)
            throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        super.setBandLevel(band, level);
        personalized = true;
        settings.set(band, level);
    }

    @Override
    public void usePreset(short preset) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        super.usePreset(preset);
        this.preset = preset;
        Log.i(TAG, "Use preset " + preset);
    }

    public void reset() {
        if (preset == -1) {
            preset = (short) 0;
        }
        usePreset(preset);
        personalized = false;
    }

    public boolean isPersonalized() {
        return personalized;
    }

    public List<Integer> getCenterFrequencies() {
        List<Integer> centerFrequencies = new ArrayList<Integer>();
        for (short i = 0; i < getNumberOfBands(); i++) {
            centerFrequencies.add(getCenterFreq(i));
        }
        return centerFrequencies;
    }

    public short getMinBandLevel() {
        return getBandLevelRange()[0];
    }

    public short getMaxBandLevel() {
        return getBandLevelRange()[1];
    }

    public List<String> getPresetNames() {
        List<String> presetNames = new ArrayList<String>();
        for (short i = 0; i < getNumberOfPresets(); i++) {
            presetNames.add(getPresetName(i));
        }
        return presetNames;
    }
}

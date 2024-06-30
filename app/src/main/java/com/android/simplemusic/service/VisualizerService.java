package com.android.simplemusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.util.Log;

import com.android.simplemusic.definition.Definition;
import com.android.simplemusic.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

public class VisualizerService extends Service {
    public static final String TAG = VisualizerService.class.getSimpleName();
    private Visualizer visualizer;
    private byte[] waveformData;
    private byte[] fftData;

    public VisualizerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        // 注册EventBus
        EventBus.getDefault().register(this);

    }

    public byte[] getWaveformData() {
        return waveformData;
    }

    public byte[] getFftData() {
        return fftData;
    }

    public void initializeVisualizer() {
        // 初始化visualizer
        visualizer = new Visualizer(0);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                waveformData = waveform;
                EventBus.getDefault().post(new MessageEvent(Definition.VISUALIZER_DATA_UPDATE, "WaveForm"));
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                fftData = fft;
                EventBus.getDefault().post(new MessageEvent(Definition.VISUALIZER_DATA_UPDATE, "Fft"));
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
        visualizer.setEnabled(true);
        Log.i(TAG, "Visualizer initialized");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
        String message = messageEvent.getMessage();
        if (message != null) {
            Log.i(TAG, "Received EventBus message " + message);
            switch (message) {
                case Definition.PERMISSION_ACQUIRED:
                    String content = (String) messageEvent.getContent();
                    if (Objects.equals(content, Definition.RECORD_AUDIO)) {
                        if (visualizer == null) {
                            initializeVisualizer();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
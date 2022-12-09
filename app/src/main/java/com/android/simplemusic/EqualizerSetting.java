package com.android.simplemusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class EqualizerSetting extends AppCompatActivity {
    private int UiMode;
    Data app;

    private LinearLayout linearLayout;
    private Toolbar toolBar3;
    private TextView toolBar3_text;
    private ArrayList<SeekBar> seekBars;
    private ArrayList<String> eqPresets;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar_background, getTheme()));
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // 绘制UI
        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // Toolbar
        toolBar3 = new Toolbar(this);
        toolBar3.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        toolBar3.setBackgroundColor(getColor(R.color.toolbar_background));
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            toolBar3.setNavigationIcon(R.drawable.arrow_back_black);
        } else {
            toolBar3.setNavigationIcon(R.drawable.arrow_back_white);
        }
        toolBar3.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        toolBar3_text = new TextView(this);
        toolBar3_text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        toolBar3_text.setText(R.string.equalizer);
        toolBar3_text.setTextColor(getColor(R.color.text));
        toolBar3_text.setTextSize(20);
        toolBar3.addView(toolBar3_text);
        linearLayout.addView(toolBar3);
        // 获取MediaPlayer
        app = (Data) getApplication();
        if (app.mediaPlayer == null) {
            app.mediaPlayer = new MediaPlayer();
            Log.i("MediaPlayer", "Created a new media player");
        }
        // 获取Equalizer
        if (app.equalizer == null) {
            app.equalizer = new Equalizer(0, app.mediaPlayer.getAudioSessionId());
            Log.i("Equalizer", "Created a new equalizer");
        }
        app.equalizer.setEnabled(true);
        short bands = app.equalizer.getNumberOfBands();
        final short minEqualizer = app.equalizer.getBandLevelRange()[0];
        final short maxEqualizer = app.equalizer.getBandLevelRange()[1];
        // 预设设置行
        // “使用预设”提示
        LinearLayout linearLayout_ps = new LinearLayout(this);
        LinearLayout.LayoutParams lp_ps = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp_ps.setMargins(20, 10, 20, 10);
        linearLayout_ps.setLayoutParams(lp_ps);
        linearLayout_ps.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView_ps = new TextView(this);
        LinearLayout.LayoutParams lp_ps_tv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp_ps_tv.setMargins(0, 0, 20, 0);
        textView_ps.setLayoutParams(lp_ps_tv);
        textView_ps.setText(R.string.use_preset);
        textView_ps.setTextSize(16);
        linearLayout_ps.addView(textView_ps);
        // 预设下拉列表
        Spinner spinner_ps = new Spinner(this);
        spinner_ps.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        short presets = app.equalizer.getNumberOfPresets();
        eqPresets = new ArrayList<String>();
        for (short i = 0; i < presets; i++) {
            eqPresets.add(app.equalizer.getPresetName(i));
        }
        ArrayAdapter<String> eqSpAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_select, eqPresets);
        eqSpAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinner_ps.setAdapter(eqSpAdapter);
        if (app.curEqSet != -1) {
            spinner_ps.setSelection((int) app.curEqSet);
        } else {
            spinner_ps.setSelection((int) app.equalizer.getCurrentPreset());
        }
        linearLayout_ps.addView(spinner_ps);
        linearLayout.addView(linearLayout_ps);
        // 自定义区域
        seekBars = new ArrayList<SeekBar>();
        for (short i = 0; i < bands; i++) {
            final short band = i;
            LinearLayout oneFreq = new LinearLayout(this);
            oneFreq.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp_oneFreq = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp_oneFreq.setMargins(20, 10, 20, 10);
            oneFreq.setLayoutParams(lp_oneFreq);
            TextView textView1 = new TextView(this);
            textView1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView1.setGravity(Gravity.CENTER_HORIZONTAL);
            textView1.setText((app.equalizer.getCenterFreq(band) / 1000) + "Hz");
            textView1.setTextSize(16);
            textView1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textView1.setWidth(180);
            oneFreq.addView(textView1);
            TextView textView2 = new TextView(this);
            textView2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView2.setGravity(Gravity.CENTER_HORIZONTAL);
            textView2.setText(minEqualizer / 100 + "dB");
            oneFreq.addView(textView2);
            SeekBar seekBar = new SeekBar(this);
            seekBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                seekBar.setMaxHeight(10);
                seekBar.setMinHeight(10);
            }
            seekBar.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.seekbar_progress));
            seekBar.setThumb(AppCompatResources.getDrawable(this, R.drawable.pause_circle_black));
            seekBar.setMax((int) (maxEqualizer - minEqualizer));
            Log.i("Equalizer", "band " + String.valueOf((int) band) + " is " + String.valueOf((int) app.equalizer.getBandLevel(band)));
            seekBar.setProgress(app.equalizer.getBandLevel(band) - minEqualizer);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.i("Equalizer", "set band " + String.valueOf((int) band) + "to " + String.valueOf((int) progress) + "+" + String.valueOf((int) minEqualizer));
                    app.equalizer.setBandLevel(band, (short) (progress + minEqualizer));
                }
            });
            oneFreq.addView(seekBar);
            seekBars.add(seekBar);
            TextView textView3 = new TextView(this);
            textView3.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView3.setGravity(Gravity.CENTER_HORIZONTAL);
            textView3.setText(maxEqualizer / 100 + "dB");
            oneFreq.addView(textView3);
            linearLayout.addView(oneFreq);
        }
        spinner_ps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                app.curEqSet = (short) position;
                app.equalizer.usePreset((short) position);
                for (short i = 0; i < bands; i++) {
                    seekBars.get((int) i).setProgress(app.equalizer.getBandLevel(i) - minEqualizer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button button1 = new Button(this);
        LinearLayout.LayoutParams lp_button1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp_button1.gravity = Gravity.CENTER_HORIZONTAL;
        button1.setLayoutParams(lp_button1);
        button1.setText(getString(R.string.reset));
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.curEqSet != -1) {
                    app.equalizer.usePreset((short) app.curEqSet);
                } else {
                    app.equalizer.usePreset((short) Equalizer.CONTENT_TYPE_MUSIC);
                }
                Log.i("Equalizer", "Number of presets is " + String.valueOf((int) app.equalizer.getNumberOfPresets()));
                for (short i = 0; i < bands; i++) {
                    seekBars.get((int) i).setProgress(app.equalizer.getBandLevel(i) - minEqualizer);
                }
            }
        });
        linearLayout.addView(button1);
        setContentView(linearLayout);
    }
}
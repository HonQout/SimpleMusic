package com.android.simplemusic.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.simplemusic.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class EqualizerDialog {
    public static final String TAG = EqualizerDialog.class.getSimpleName();
    private Context context;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private List<String> presets;
    private short selection = -1;
    private short numBands = -1;
    private int minBandLevel = -1;
    private List<Integer> centerFreq;
    private int maxBandLevel = -1;

    public EqualizerDialog(@NonNull Context context) {
        this.context = context;
    }

    public EqualizerDialog setPresets(List<String> presets) {
        this.presets = presets;
        return this;
    }

    public EqualizerDialog setSelection(short selection) {
        this.selection = selection;
        return this;
    }

    public EqualizerDialog setNumBands(short numBands) {
        this.numBands = numBands;
        return this;
    }

    public EqualizerDialog setMinBandLevel(int minBandLevel) {
        this.minBandLevel = minBandLevel;
        return this;
    }

    public EqualizerDialog setCenterFrequencies(List<Integer> centerFreq) {
        this.centerFreq = centerFreq;
        return this;
    }

    public EqualizerDialog setMaxBandLevel(int maxBandLevel) {
        this.maxBandLevel = maxBandLevel;
        return this;
    }

    public AlertDialog create() {
        builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.equalizer);
        builder.setTitle(context.getString(R.string.equalizer));
        // 根layout
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // “使用预设”行
        View top = View.inflate(context, R.layout.equalizer_top, null);
        TextView textView1 = top.findViewById(R.id.equalizer_top_hint);
        Spinner spinner = top.findViewById(R.id.equalizer_top_spinner);
        textView1.setText(R.string.use_preset);
        if (presets == null) {
            throw new IllegalArgumentException();
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(context, R.layout.spinner_item_select, presets);
        adapter1.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinner.setAdapter(adapter1);
        if (selection == -1) {
            throw new IllegalArgumentException();
        }
        Log.i(TAG, "Current Preset is " + String.valueOf(selection));
        spinner.setSelection(selection);
        linearLayout.addView(top);
        // 各频段均衡器设置部分
        if (numBands == -1 || centerFreq == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<SeekBar> seekBars = new ArrayList<SeekBar>();
        for (short i = 0; i < numBands; i++) {
            final short band = i;
            View unit = View.inflate(context, R.layout.equalizer_unit, null);
            TextView textView2 = unit.findViewById(R.id.equalizer_unit_minEq);
            TextView textView3 = unit.findViewById(R.id.equalizer_unit_freq);
            TextView textView4 = unit.findViewById(R.id.equalizer_unit_maxEq);
            SeekBar seekBar = unit.findViewById(R.id.equalizer_unit_seekbar);
            textView2.setText(String.format(Locale.US, "%ddB", minBandLevel / 100));
            textView3.setText(String.format(Locale.US, "%dkHz", centerFreq.get(i) / 1000));
            textView4.setText(String.format(Locale.US, "%ddB", maxBandLevel / 100));
            Log.i(TAG, String.format("band %d is %dkHz", (int) i, (int) centerFreq.get(i) / 1000));
            seekBar.setMax((int) maxBandLevel - minBandLevel);
            seekBar.setProgress(centerFreq.get(i) - minBandLevel);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        onSeekBarChange(band, (short) (progress + minBandLevel));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBars.add(seekBar);
            linearLayout.addView(unit);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSpinnerChange(position, seekBars, minBandLevel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setView(linearLayout);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onResetClick(seekBars, minBandLevel);
            }
        });
        builder.setCancelable(false);
        dialog = builder.create();
        return dialog;
    }

    public abstract void onSeekBarChange(short band, short level);

    public abstract void onSpinnerChange(int position, ArrayList<SeekBar> seekBars, int minBandLevel);

    public abstract void onResetClick(ArrayList<SeekBar> seekBars, int minBandLevel);
}

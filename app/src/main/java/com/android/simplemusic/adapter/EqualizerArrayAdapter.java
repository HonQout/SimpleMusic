package com.android.simplemusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.simplemusic.R;

import java.util.List;

public class EqualizerArrayAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> objects;

    public EqualizerArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.equalizer_item, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.equalizer_item_text);
        textView.setText(objects.get(position));
        return convertView;
    }
}

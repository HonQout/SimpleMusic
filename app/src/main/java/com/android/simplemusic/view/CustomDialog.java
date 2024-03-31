package com.android.simplemusic.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.utils.MusicUtils;

public class CustomDialog {
    public static void MusicInfoDialog(@NonNull Context context, @NonNull Music music) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.music_info);
        View view = View.inflate(context, R.layout.dialog_music_info, null);
        TextView mid_name = view.findViewById(R.id.mid_name_content);
        TextView mid_artist = view.findViewById(R.id.mid_artist_content);
        TextView mid_album = view.findViewById(R.id.mid_album_content);
        TextView mid_bitrate = view.findViewById(R.id.mid_bitrate_content);
        TextView mid_cfr = view.findViewById(R.id.mid_cfr_content);
        TextView mid_duration = view.findViewById(R.id.mid_duration_content);
        TextView mid_path = view.findViewById(R.id.mid_path_content);
        TextView mid_size = view.findViewById(R.id.mid_size_content);
        mid_name.setText(music.getTitle());
        mid_artist.setText(music.getArtist());
        mid_album.setText(music.getAlbum());
        mid_bitrate.setText(MusicUtils.formatBitrate(music.getBitrate()));
        mid_cfr.setText(MusicUtils.formatCaptureFrameRate(music.getCaptureFrameRate()));
        mid_duration.setText(MusicUtils.formatDuration(music.getDuration()));
        mid_path.setText(music.getPath());
        mid_size.setText(MusicUtils.formatSize(music.getSize()));
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, null);
        builder.create().show();
    }
}
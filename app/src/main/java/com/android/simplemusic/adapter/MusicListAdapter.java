package com.android.simplemusic.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.simplemusic.utils.MusicUtils;
import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends ArrayAdapter<Music> implements Filterable {
    private final int resourceId;
    List<Music> musicList;
    List<Music> musicList_bak;
    MusicFilter musicFilter;

    static class ViewHolder {
        ImageView musicImage;
        TextView musicName;
        TextView musicInfo;

        public ViewHolder(ImageView musicImage, TextView musicName, TextView musicInfo) {
            this.musicImage = musicImage;
            this.musicName = musicName;
            this.musicInfo = musicInfo;
        }
    }

    public MusicListAdapter(Context context, int resourceId, List<Music> musicList) {
        super(context, resourceId, musicList);
        this.resourceId = resourceId;
        this.musicList = musicList;
        this.musicList_bak = musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
        this.musicList_bak = musicList;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Music getItem(int position) {
        if (position < musicList.size()) {
            return musicList.get(position);
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Music music = musicList.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            ImageView musicImage = view.findViewById(R.id.musicImage);
            TextView musicName = view.findViewById(R.id.musicName);
            TextView musicSinger = view.findViewById(R.id.musicInfo);
            viewHolder = new ViewHolder(musicImage, musicName, musicSinger);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (music != null) {
            viewHolder.musicImage.setImageResource(R.drawable.music_note);
            viewHolder.musicName.setText(music.getTitle());
            viewHolder.musicInfo.setText(String.format(music.getArtist() + "   " + MusicUtils.formatTime(music.getDuration())));
        }
        return view;
    }

    class MusicFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Music> list;
            if (TextUtils.isEmpty(charSequence)) {
                list = musicList_bak;
            } else {
                charSequence = charSequence.toString().toLowerCase();
                list = new ArrayList<Music>();
                for (Music item : musicList_bak) {
                    if (item.getTitle().toLowerCase().contains(charSequence) || item.getArtist().toLowerCase().contains(charSequence)) {
                        list.add(item);
                    }
                }
            }
            result.values = list;
            result.count = list.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            List<?> resultList = (List<?>) filterResults.values;
            musicList = new ArrayList<Music>();
            for (Object item : resultList) {
                musicList.add((Music) item);
            }
            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (musicFilter == null) {
            musicFilter = new MusicFilter();
        }
        return musicFilter;
    }
}
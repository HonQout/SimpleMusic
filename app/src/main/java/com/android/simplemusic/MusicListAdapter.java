package com.android.simplemusic;

import android.annotation.SuppressLint;
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
            viewHolder.musicName.setText(music.getName());
            viewHolder.musicInfo.setText(music.getSinger() + "   " + MusicUtils.formatTime(music.getDuration()));
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
                list = new ArrayList<Music>();
                for (Music item : musicList_bak) {
                    if (item.getName().contains(charSequence) || item.getSinger().contains(charSequence)) {
                        list.add(item);
                    }
                }
            }
            result.values = list;
            result.count = list.size();
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            try {
                musicList = (List<Music>) filterResults.values;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    @Override
    public Filter getFilter() {
        if (musicFilter == null) {
            musicFilter = new MusicFilter();
        }
        return musicFilter;
    }
}


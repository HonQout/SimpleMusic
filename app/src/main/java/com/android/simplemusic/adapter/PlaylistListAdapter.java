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

import com.android.simplemusic.R;
import com.android.simplemusic.bean.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistListAdapter extends ArrayAdapter<Playlist> implements Filterable {
    private final int resourceId;
    List<Playlist> playlistList;
    List<Playlist> playlistList_bak;
    PlaylistFilter playlistFilter;

    static class ViewHolder {
        ImageView playlistImage;
        TextView playlistName;

        public ViewHolder(ImageView playlistImage, TextView playlistName) {
            this.playlistImage = playlistImage;
            this.playlistName = playlistName;
        }
    }

    public PlaylistListAdapter(Context context, int resourceId, List<Playlist> playlistList) {
        super(context, resourceId, playlistList);
        this.resourceId = resourceId;
        this.playlistList = playlistList;
        this.playlistList_bak = playlistList;
    }

    public void setPlaylistList(List<Playlist> playlistList) {
        this.playlistList = playlistList;
        this.playlistList_bak = playlistList;
    }

    @Override
    public int getCount() {
        return playlistList.size();
    }

    @Override
    public Playlist getItem(int position) {
        if (position < playlistList.size()) {
            return playlistList.get(position);
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Playlist playlist = playlistList.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            ImageView playlistImage = view.findViewById(R.id.playlistImage);
            TextView playlistName = view.findViewById(R.id.playlistName);
            viewHolder = new ViewHolder(playlistImage, playlistName);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (playlist != null) {
            viewHolder.playlistImage.setImageResource(R.drawable.library_music_black);
            viewHolder.playlistName.setText(playlist.getTitle());
        }
        return view;
    }

    class PlaylistFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Playlist> list;
            if (TextUtils.isEmpty(charSequence)) {
                list = playlistList_bak;
            } else {
                list = new ArrayList<Playlist>();
                for (Playlist item : playlistList_bak) {
                    if (item.getTitle().contains(charSequence)) {
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
            playlistList = new ArrayList<Playlist>();
            for (Object item : resultList) {
                playlistList.add((Playlist) item);
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
        if (playlistFilter == null) {
            playlistFilter = new PlaylistFilter();
        }
        return playlistFilter;
    }
}

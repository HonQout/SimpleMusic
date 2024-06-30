package com.android.simplemusic.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.simplemusic.room.entity.Playlist;

import java.util.List;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    public static final String TAG = PlaylistRecyclerAdapter.class.getSimpleName();

    private Context context;
    private List<Playlist> playlistList;
    private List<Playlist> playlistList_bak;
    //private ItemClickListener itemClickListener;
    //private PlaylistFilter playlistFilter;

    public PlaylistRecyclerAdapter(Context context, List<Playlist> playlistList) {
        this.context = context;
        this.playlistList = playlistList;
        this.playlistList_bak = playlistList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return null;
    }


    @Override
    public Filter getFilter() {
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

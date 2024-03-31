package com.android.simplemusic.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.android.simplemusic.R;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.utils.MusicDiffUtils;
import com.android.simplemusic.utils.MusicUtils;

import java.util.ArrayList;
import java.util.List;

public class MusicRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private Context context;
    private List<Music> musicList;
    private List<Music> musicList_bak;
    private ItemClickListener itemClickListener;
    private MusicFilter musicFilter;

    public MusicRecyclerAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
        this.musicList_bak = musicList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        Music music = musicList.get(position);
        itemHolder.musicImage.setImageResource(R.drawable.music_note);
        itemHolder.musicName.setText(music.getTitle());
        itemHolder.musicInfo.setText(String.format("%s | %s", music.getArtist(), music.getAlbum()));
        itemHolder.musicDuration.setText(MusicUtils.formatDuration(music.getDuration()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemClickListener.onItemLongClick(holder.getAdapterPosition());
                return true;
            }
        });
    }

    public void setMusicList(List<Music> musicList) {
        List<Music> oldList = this.musicList_bak;
        if (this.musicList.size() == this.musicList_bak.size()) { //未在搜索状态下
            this.musicList = musicList;
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MusicDiffUtils(oldList, musicList));
            diffResult.dispatchUpdatesTo(this);
        }
        this.musicList_bak = musicList;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public Music getItem(int position) {
        if (position >= 0 && position < musicList.size()) {
            return musicList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return musicList == null ? -1 : musicList.size();
    }

    @Override
    public Filter getFilter() {
        if (musicFilter == null) {
            musicFilter = new MusicFilter();
        }
        return musicFilter;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView musicImage;
        public TextView musicName;
        public TextView musicInfo;
        public TextView musicDuration;


        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            musicImage = itemView.findViewById(R.id.musicImage);
            musicName = itemView.findViewById(R.id.musicName);
            musicInfo = itemView.findViewById(R.id.musicInfo);
            musicDuration = itemView.findViewById(R.id.musicDuration);
        }
    }

    public interface ItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    class MusicFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            List<Music> resultList;
            if (TextUtils.isEmpty(constraint)) {
                resultList = musicList_bak;
            } else {
                constraint = constraint.toString().toLowerCase();
                resultList = new ArrayList<Music>();
                for (Music item : musicList_bak) {
                    boolean isTitleMatch = item.getTitle().toLowerCase().contains(constraint);
                    boolean isArtistMatch = item.getArtist().toLowerCase().contains(constraint);
                    boolean isAlbumMatch = item.getAlbum().toLowerCase().contains(constraint);
                    if (isTitleMatch || isArtistMatch || isAlbumMatch) {
                        resultList.add(item);
                    }
                }
            }
            result.values = resultList;
            result.count = resultList.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<?> resultList = (List<?>) results.values;
            List<Music> oldList = musicList;
            musicList = new ArrayList<Music>();
            for (Object item : resultList) {
                musicList.add((Music) item);
            }
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MusicDiffUtils(oldList, musicList));
            diffResult.dispatchUpdatesTo(MusicRecyclerAdapter.this);
        }
    }
}

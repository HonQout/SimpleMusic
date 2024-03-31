package com.android.simplemusic.utils;

import androidx.recyclerview.widget.DiffUtil;

import com.android.simplemusic.bean.Music;

import java.util.List;

public class MusicDiffUtils extends DiffUtil.Callback {
    private final List<Music> oldList;
    private final List<Music> newList;

    public MusicDiffUtils(List<Music> oldList, List<Music> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null ? -1 : oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList == null ? -1 : newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Music item1 = oldItemPosition >= 0 && oldItemPosition < oldList.size() ? oldList.get(oldItemPosition) : null;
        Music item2 = newItemPosition >= 0 && newItemPosition < newList.size() ? newList.get(newItemPosition) : null;
        if (item1 == null || item2 == null) {
            return false;
        } else {
            return item1.equals(item2);
        }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Music item1 = oldItemPosition >= 0 && oldItemPosition < oldList.size() ? oldList.get(oldItemPosition) : null;
        Music item2 = newItemPosition >= 0 && newItemPosition < newList.size() ? newList.get(newItemPosition) : null;
        if (item1 == null || item2 == null) {
            return false;
        } else {
            boolean isTitleTheSame = item1.getTitle().equals(item2.getTitle());
            boolean isArtistTheSame = item1.getArtist().equals(item2.getArtist());
            boolean isAlbumTheSame = item1.getAlbum().equals(item2.getAlbum());
            boolean isDurationTheSame = item1.getDuration() == item2.getDuration();
            return isTitleTheSame && isArtistTheSame && isAlbumTheSame && isDurationTheSame;
        }
    }
}

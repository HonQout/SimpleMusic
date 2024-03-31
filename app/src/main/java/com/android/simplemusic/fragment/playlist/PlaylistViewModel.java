package com.android.simplemusic.fragment.playlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.simplemusic.entity.Playlist;

import java.util.List;

public class PlaylistViewModel extends ViewModel {
    private final MutableLiveData<List<Playlist>> mPlaylists;

    public PlaylistViewModel() {
        mPlaylists = new MutableLiveData<List<Playlist>>();
    }

    public void setPlaylists(List<Playlist> playlists) {
        mPlaylists.postValue(playlists);
    }

    public List<Playlist> getPlaylists() {
        return mPlaylists.getValue();
    }
}
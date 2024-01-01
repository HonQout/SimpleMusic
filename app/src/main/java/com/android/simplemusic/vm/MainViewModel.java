package com.android.simplemusic.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.simplemusic.dbhelper.PlaylistDBHelper;
import com.android.simplemusic.bean.Music;
import com.android.simplemusic.bean.Playlist;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<Music>> mMusicList = new MutableLiveData<>();
    private final MutableLiveData<PlaylistDBHelper> mDBHelper = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Playlist>> mPlaylists = new MutableLiveData<>();

    public void setmMusicList(List<Music> musicList) {
        mMusicList.postValue(musicList);
    }

    public MutableLiveData<List<Music>> getmMusicList() {
        return mMusicList;
    }

    public void setmDBHelper(PlaylistDBHelper dbHelper) {
        mDBHelper.postValue(dbHelper);
    }

    public MutableLiveData<PlaylistDBHelper> getmDBHelper() {
        return mDBHelper;
    }

    public void setmPlaylists(ArrayList<Playlist> playlists) {
        mPlaylists.postValue(playlists);
    }

    public MutableLiveData<ArrayList<Playlist>> getmPlaylists() {
        return mPlaylists;
    }
}

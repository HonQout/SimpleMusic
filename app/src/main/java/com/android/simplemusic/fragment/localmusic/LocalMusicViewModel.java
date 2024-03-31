package com.android.simplemusic.fragment.localmusic;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.simplemusic.bean.Music;

import java.util.List;

public class LocalMusicViewModel extends ViewModel {
    private final MutableLiveData<List<Music>> mMusicList = new MutableLiveData<List<Music>>();
    private final MutableLiveData<Integer> mIndex = new MutableLiveData<Integer>();

    public void setMusicList(List<Music> musicList) {
        mMusicList.postValue(musicList);
    }

    public MutableLiveData<List<Music>> getMusicList() {
        return mMusicList;
    }

    public void setIndex(int index) {
        mIndex.postValue(index);
    }

    public MutableLiveData<Integer> getIndex() {
        return mIndex;
    }
}
package com.android.simplemusic.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.simplemusic.bean.Music;

import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<Music>> mMusicList = new MutableLiveData<List<Music>>();
    private final MutableLiveData<Integer> mIndex = new MutableLiveData<Integer>();
    private final MutableLiveData<Bitmap> mImage = new MutableLiveData<Bitmap>();

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

    public void setImage(Bitmap image) {
        mImage.postValue(image);
    }

    public MutableLiveData<Bitmap> getImage() {
        return mImage;
    }
}
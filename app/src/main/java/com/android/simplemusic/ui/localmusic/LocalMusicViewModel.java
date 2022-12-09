package com.android.simplemusic.ui.localmusic;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocalMusicViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LocalMusicViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is local music fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
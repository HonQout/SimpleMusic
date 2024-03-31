package com.android.simplemusic.db;

import androidx.room.TypeConverter;

import com.android.simplemusic.bean.Music;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MusicItemConverter {
    @TypeConverter
    public String objectToString(List<Music> musicList) {
        return new Gson().toJson(musicList);
    }

    @TypeConverter
    public List<Music> stringToObject(String s) {
        Type listType = new TypeToken<List<Music>>() {
        }.getType();
        return new Gson().fromJson(s, listType);
    }
}

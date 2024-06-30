package com.android.simplemusic.room.entity;

import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.android.simplemusic.bean.Music;
import com.android.simplemusic.room.converter.MusicItemConverter;

import java.util.List;

@Entity(tableName = "Current")
@TypeConverters(MusicItemConverter.class)
public class Playlist1 extends Playlist {
    public static final long timeCreated = 0;
    public static final String name = "Current";
    public static final String description = "The current playlist.";

    public Playlist1(List<Music> content) {
        super(timeCreated, name, description, content);
    }
}

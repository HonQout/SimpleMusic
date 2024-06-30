package com.android.simplemusic.room.entity;

import com.android.simplemusic.bean.Music;

import java.util.List;

public class Playlist2 extends Playlist {


    public Playlist2(long timeCreated, String name, String description, List<Music> content) {
        super(timeCreated, name, description, content);
    }
}

package com.android.simplemusic.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.android.simplemusic.bean.Music;
import com.android.simplemusic.db.MusicItemConverter;

import java.util.ArrayList;
import java.util.List;

@Entity
@TypeConverters(MusicItemConverter.class)
public class Playlist {
    @PrimaryKey
    private long timeCreated; //创建时间（主键）
    private String name; //名称
    private String description; //说明
    private List<Music> content; //内容

    public Playlist(long timeCreated, String name, String description, List<Music> content) {
        this.timeCreated = timeCreated;
        this.name = name;
        this.description = description;
        this.content = content;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(List<Music> content) {
        this.content = content;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Music> getContent() {
        return content;
    }

    public boolean addMusic(int position, Music music) {
        if (content == null) {
            content = new ArrayList<Music>();
        }
        if (position >= 0 && position <= content.size()) {
            content.add(position, music);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeMusic(int position) {
        if (content == null) {
            return false;
        } else {
            if (position >= 0 && position < content.size()) {
                content.remove(position);
                return true;
            } else {
                return false;
            }
        }
    }

    public void clearList() {
        if (content == null) {
            content = new ArrayList<Music>();
        } else {
            content.clear();
        }
    }
}

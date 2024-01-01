package com.android.simplemusic.bean;

import java.util.ArrayList;

public class Playlist {
    private String title; //标题
    private ArrayList<Music> musicList; //内容

    public Playlist(String title, ArrayList<Music> musicList) {
        this.title = title;
        this.musicList = musicList;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMusicList(ArrayList<Music> musicList) {
        this.musicList = musicList;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Music> getMusicList() {
        return musicList;
    }

    public boolean addMusic(int position, Music music) {
        if (musicList == null) {
            musicList = new ArrayList<Music>();
        }
        if (position >= 0 && position <= musicList.size()) {
            musicList.add(position, music);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeMusic(int position) {
        if (musicList == null) {
            musicList = new ArrayList<Music>();
            return false;
        } else {
            if (position >= 0 && position < musicList.size()) {
                musicList.remove(position);
                return true;
            } else {
                return false;
            }
        }
    }

    public void clearList() {
        if (musicList == null) {
            musicList = new ArrayList<Music>();
        }
        musicList.clear();
    }
}

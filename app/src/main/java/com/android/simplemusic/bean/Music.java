package com.android.simplemusic.bean;

public class Music {
    private String title; //名称
    private String artist; //艺术家
    private String path; //路径
    private int duration; //时长
    private long size; //大小

    public Music(String title, String artist, String path, int duration, long size) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.size = size;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public int getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }
}

package com.android.simplemusic;

public class Music {
    private String name; //歌曲名
    private String singer; //歌手
    private String path; //路径
    private int duration; //时长
    private long size; //大小

    public Music(String name, String singer, String path, int duration, long size) {
        this.name = name;
        this.singer = singer;
        this.path = path;
        this.duration = duration;
        this.size = size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSinger(String singer) {
        this.singer = singer;
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

    public String getName() {
        return name;
    }

    public String getSinger() {
        return singer;
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

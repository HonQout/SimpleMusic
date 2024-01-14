package com.android.simplemusic.bean;

public class Music {
    private String album; //专辑
    private String artist; //艺术家
    //private int bitrate; //比特率
    //private int capture_frame_rate; //采样率
    private int duration; //时长
    private String path; //路径
    private long size; //大小
    private String title; //名称

    public Music(String title, String artist, String album, String path, int duration, long size) {
        this.title = title;
        this.artist = artist;
        this.album = album;
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

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /*public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public void setCapture_frame_rate(int capture_frame_rate) {
        this.capture_frame_rate = capture_frame_rate;
    }*/

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

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    /*public int getBitrate() {
        return bitrate;
    }

    public int getCapture_frame_rate() {
        return capture_frame_rate;
    }*/

    public int getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }
}

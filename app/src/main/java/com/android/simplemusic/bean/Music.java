package com.android.simplemusic.bean;

public class Music {
    private final String title; //名称
    private final String artist; //艺术家
    private final String album; //专辑
    private final int bitrate; //比特率
    private final float captureFrameRate; //采样率
    private final int duration; //时长
    private final String path; //路径
    private final long size; //文件大小

    public Music(String title, String artist, String album, int bitrate, float captureFrameRate,
                 int duration, String path, long size) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.bitrate = bitrate;
        this.captureFrameRate = captureFrameRate;
        this.duration = duration;
        this.path = path;
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

    public int getBitrate() {
        return bitrate;
    }

    public float getCaptureFrameRate() {
        return captureFrameRate;
    }

    public int getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }
}
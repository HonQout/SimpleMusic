package com.android.simplemusic.event;

public class MessageEvent {
    private final String message;
    private final Object content;

    public MessageEvent(String message) {
        this.message = message;
        this.content = null;
    }

    public MessageEvent(String message, Object content) {
        this.message = message;
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public Object getContent() {
        return content;
    }
}
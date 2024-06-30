package com.android.simplemusic.event;

public class MessageEvent {
    private final String sender;
    private final String receiver;
    private final String message;
    private final Object content;

    public MessageEvent(String message) {
        this(null, null, message, null);
    }

    public MessageEvent(String message, Object content) {
        this(null, null, message, content);
    }

    public MessageEvent(String sender, String receiver, String message, Object content) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public Object getContent() {
        return content;
    }
}
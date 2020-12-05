package com.codemountain.slicker.model;

public class ModelChat {

    private String sender, receiver, timestamp, message;
    private boolean isSeen;

    public ModelChat() {
    }

    public ModelChat(String sender, String receiver, String timestamp, String message, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.message = message;
        this.isSeen = isSeen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}

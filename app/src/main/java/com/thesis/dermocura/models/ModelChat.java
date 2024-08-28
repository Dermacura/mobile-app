package com.thesis.dermocura.models;

public class ModelChat {
    private String text;
    private String time;
    private boolean isOutgoing;

    public ModelChat(String text, String time, boolean isOutgoing) {
        this.text = text;
        this.time = time;
        this.isOutgoing = isOutgoing;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }
}

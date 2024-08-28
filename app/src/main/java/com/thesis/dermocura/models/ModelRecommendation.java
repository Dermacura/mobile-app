package com.thesis.dermocura.models;

public class ModelRecommendation {
    private String title;
    private String content;

    public ModelRecommendation(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}

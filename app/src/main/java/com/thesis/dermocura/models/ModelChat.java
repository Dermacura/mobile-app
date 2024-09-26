package com.thesis.dermocura.models;

public class ModelChat {
    private String text;
    private String time;
    private boolean isOutgoing;
    private String profileImageUrl;  // Add a field for the profile image URL

    public ModelChat(String text, String time, boolean isOutgoing, String profileImageUrl) {
        this.text = text;
        this.time = time;  // This now includes both date and time
        this.isOutgoing = isOutgoing;
        this.profileImageUrl = profileImageUrl;  // Store the profile image URL
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

    public String getProfileImageUrl() {
        return profileImageUrl;  // Getter for profile image URL
    }
}

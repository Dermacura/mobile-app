package com.thesis.dermocura.models;

public class ModelSMS {
    private int telemedicineID;
    private int userAccID; // New field for userAccID
    private String tempFullName;
    private String telemedicineContent;
    private String formattedDate;
    private String profile; // New field for profile

    public ModelSMS(int telemedicineID, int userAccID, String tempFullName, String telemedicineContent, String formattedDate, String profile) {
        this.telemedicineID = telemedicineID;
        this.userAccID = userAccID;
        this.tempFullName = tempFullName;
        this.telemedicineContent = telemedicineContent;
        this.formattedDate = formattedDate;
        this.profile = profile;
    }

    public int getTelemedicineID() {
        return telemedicineID;
    }

    public int getUserAccID() {
        return userAccID;
    }

    public String getTempFullName() {
        return tempFullName;
    }

    public String getTelemedicineContent() {
        return telemedicineContent;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getProfile() {
        return "https://clinics.dermocura.net/assets/images/user_profile/" + profile;
    }
}


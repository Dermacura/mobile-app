package com.thesis.dermocura.datas;

public class UserData {
    private String patientEmail;
    private int patientID;
    private String patientImageURL;
    private String patientMobileNumber;
    private String patientName;

    // Constructor
    public UserData(String patientEmail, int patientID, String patientImageURL, String patientMobileNumber, String patientName) {
        this.patientEmail = patientEmail;
        this.patientID = patientID;
        this.patientImageURL = patientImageURL;
        this.patientMobileNumber = patientMobileNumber;
        this.patientName = patientName;
    }

    // Getters and Setters
    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getPatientImageURL() {
        return "https://backend.dermocura.net/images/user_profile/" + patientImageURL;
    }

    public void setPatientImageURL(String patientImageURL) {
        this.patientImageURL = patientImageURL;
    }

    public String getPatientMobileNumber() {
        return patientMobileNumber;
    }

    public void setPatientMobileNumber(String patientMobileNumber) {
        this.patientMobileNumber = patientMobileNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}

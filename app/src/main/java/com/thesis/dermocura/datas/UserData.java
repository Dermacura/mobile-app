package com.thesis.dermocura.datas;

public class UserData {
    private String patientEmail;
    private int patientID;
    private String patientImageURL;  // This should store only the relative path or filename
    private String patientMobileNumber;
    private String patientName;
    private int patientAge;  // New field for age
    private String patientGender;  // New field for gender

    // Constructor
    public UserData(String patientEmail, int patientID, String patientImageURL, String patientMobileNumber, String patientName, int patientAge, String patientGender) {
        this.patientEmail = patientEmail;
        this.patientID = patientID;
        this.patientImageURL = patientImageURL;
        this.patientMobileNumber = patientMobileNumber;
        this.patientName = patientName;
        this.patientAge = patientAge;  // Initialize new field
        this.patientGender = patientGender;  // Initialize new field
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
        return patientImageURL != null ? "https://backend.dermocura.net/images/user_profile/" + patientImageURL : null;
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

    public int getPatientAge() {  // Getter for patientAge
        return patientAge;
    }

    public void setPatientAge(int patientAge) {  // Setter for patientAge
        this.patientAge = patientAge;
    }

    public String getPatientGender() {  // Getter for patientGender
        return patientGender;
    }

    public void setPatientGender(String patientGender) {  // Setter for patientGender
        this.patientGender = patientGender;
    }
}

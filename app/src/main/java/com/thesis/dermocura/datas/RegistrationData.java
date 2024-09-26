package com.thesis.dermocura.datas;

public class RegistrationData {
    private static RegistrationData instance;

    // Registration fields based on your database table
    private String patientName;
    private String patientPassword;
    private String patientEmail;
    private String patientMobileNumber;
    private String patientImageURL;
    private String patientGender;
    private Integer patientAge;
    private String patientBirthDate;  // Changed to String to match the YYYY-MM-DD format

    // Private constructor to prevent direct instantiation
    private RegistrationData() {}

    // Get the singleton instance
    public static RegistrationData getInstance() {
        if (instance == null) {
            instance = new RegistrationData();
        }
        return instance;
    }

    // Getter and Setter methods for each field

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPassword() {
        return patientPassword;
    }

    public void setPatientPassword(String patientPassword) {
        this.patientPassword = patientPassword;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientMobileNumber() {
        return patientMobileNumber;
    }

    public void setPatientMobileNumber(String patientMobileNumber) {
        this.patientMobileNumber = patientMobileNumber;
    }

    public String getPatientImageURL() {
        return patientImageURL;
    }

    public void setPatientImageURL(String patientImageURL) {
        this.patientImageURL = patientImageURL;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;  // Set as String in YYYY-MM-DD format
    }

    // Clear all data method
    public void clearData() {
        patientName = null;
        patientPassword = null;
        patientEmail = null;
        patientMobileNumber = null;
        patientImageURL = null;
        patientGender = null;
        patientAge = null;
        patientBirthDate = null;
    }
}

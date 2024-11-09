package com.thesis.dermocura.models;

public class Appointment {
    private int appointmentID;
    private String doctorName;
    private String clinicName;
    private String clinicLogo; // Add clinic logo for the image
    private String startTime;
    private String endTime;
    private String availDate;
    private String status;
    private String remarkInput;

    // Constructor updated to accept remarkInput
    public Appointment(int appointmentID, String doctorName, String clinicName, String clinicLogo,
                       String startTime, String endTime, String availDate, String status, String remarkInput) {
        this.appointmentID = appointmentID;
        this.doctorName = doctorName;
        this.clinicName = clinicName;
        this.clinicLogo = clinicLogo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availDate = availDate;
        this.status = status;
        this.remarkInput = remarkInput; // Initialize the remarkInput field
    }

    // Getters and Setters
    public int getAppointmentID() {
        return appointmentID;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getClinicName() {
        return clinicName;
    }

    public String getClinicLogo() {
        return clinicLogo;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getAvailDate() {
        return availDate;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarkInput() {
        return remarkInput;
    }

    public void setRemarkInput(String remarkInput) {
        this.remarkInput = remarkInput;
    }
}

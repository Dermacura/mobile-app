package com.thesis.dermocura.models;

public class ModelDoctor {
    private int doctorID;
    private String doctorName;
    private String doctorEmail;
    private String doctorImage;

    public ModelDoctor(int doctorID, String doctorName, String doctorEmail, String doctorImage) {
        this.doctorID = doctorID;
        this.doctorName = doctorName;
        this.doctorEmail = doctorEmail;
        this.doctorImage = doctorImage;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public String getDoctorImage() {
        return doctorImage;
    }
}

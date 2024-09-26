package com.thesis.dermocura.models;

public class ModelContacts {
    private int userAccID;
    private String fullname;
    private String clinicName;
    private String profile;

    public ModelContacts(int userAccID, String fullname, String clinicName, String profile) {
        this.userAccID = userAccID;
        this.fullname = fullname;
        this.clinicName = clinicName;
        this.profile = profile;
    }

    public int getUserAccID() {
        return userAccID;
    }

    public String getFullname() {
        return fullname;
    }

    public String getClinicName() {
        return clinicName;
    }

    public String getProfile() {
        return profile;
    }
}


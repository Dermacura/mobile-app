package com.thesis.dermocura.models;

public class ModelHistory {
    private int skinDiseaseID;
    private String skinDiseaseName;
    private String skinDiseaseImageURL;
    private String patientAnalyzedDate;

    public ModelHistory(int skinDiseaseID, String skinDiseaseName, String skinDiseaseImageURL, String patientAnalyzedDate) {
        this.skinDiseaseID = skinDiseaseID;
        this.skinDiseaseName = skinDiseaseName;
        this.skinDiseaseImageURL = skinDiseaseImageURL;
        this.patientAnalyzedDate = patientAnalyzedDate;
    }

    public int getSkinDiseaseID() {
        return skinDiseaseID;
    }

    public String getSkinDiseaseName() {
        return skinDiseaseName;
    }

    public String getSkinDiseaseImageURL() {
        return skinDiseaseImageURL;
    }

    public String getPatientAnalyzedDate() {
        return patientAnalyzedDate;
    }
}

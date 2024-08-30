package com.thesis.dermocura.datas;

public class LocationData {
    private double latitude;
    private double longitude;
    private String city;
    private String name;

    public LocationData(double latitude, double longitude, String city, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCity() {
        return city;
    }

    public String getName() {
        return name;
    }
}
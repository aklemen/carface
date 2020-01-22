package com.example.carface;


public class RadioStation {
    private int id;
    private String stationName;
    private String stationFrequency;
    private String stationUrl;

    public RadioStation(int id, String name, String frequency, String url) {
        this.id = id;
        this.stationName = name;
        this.stationFrequency = frequency;
        this.stationUrl = url;
    }

    public int getId() {
        return id;
    }

    public String getStationName() {
        return stationName;
    }

    public String getStationFrequency() {
        return stationFrequency;
    }

    public String getStationUrl() {
        return stationUrl;
    }
}
package com.example.coen_390_app;

public class RoomData {

    private int roomId;
    private double temperature;
    private double humidity;
    private double co2Level;
    private double dustLevel;
    private double tvoc;
    private double co;
    private double alcohol;
    private double nh4;
    private double acetone;
    private double propane;
    private double h2;
    private double toluene;

    public RoomData(int roomId, double temperature, double humidity, double co2Level, double dustLevel,
                    double tvoc, double co, double alcohol, double nh4, double acetone,
                    double propane, double h2, double toluene) {
        this.roomId = roomId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2Level = co2Level;
        this.dustLevel = dustLevel;
        this.tvoc = tvoc;
        this.co = co;
        this.alcohol = alcohol;
        this.nh4 = nh4;
        this.acetone = acetone;
        this.propane = propane;
        this.h2 = h2;
        this.toluene = toluene;
    }

    public int getRoomId() {
        return roomId;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getCo2Level() {
        return co2Level;
    }

    public double getDustLevel() {
        return dustLevel;
    }

    public double getTvoc() {
        return tvoc;
    }

    public double getCo() {
        return co;
    }

    public double getAlcohol() {
        return alcohol;
    }

    public double getNh4() {
        return nh4;
    }

    public double getAcetone() {
        return acetone;
    }

    public double getPropane() {
        return propane;
    }

    public double getH2() {
        return h2;
    }

    public double getToluene() {
        return toluene;
    }

    @Override
    public String toString() {
        return "RoomData{" +
                "roomId=" + roomId +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", co2Level=" + co2Level +
                ", dustLevel=" + dustLevel +
                ", tvoc=" + tvoc +
                ", co=" + co +
                ", alcohol=" + alcohol +
                ", nh4=" + nh4 +
                ", acetone=" + acetone +
                ", propane=" + propane +
                ", h2=" + h2 +
                ", toluene=" + toluene +
                '}';
    }
}

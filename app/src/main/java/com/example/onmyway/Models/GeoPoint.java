package com.example.onmyway.Models;


public class GeoPoint {

    private double speed;
    private long time;
    private double latitude;
    private double longitude;
    public GeoPoint() { }

    public GeoPoint(double speed, long time, double latitude, double longitude) {
        this.speed = speed;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

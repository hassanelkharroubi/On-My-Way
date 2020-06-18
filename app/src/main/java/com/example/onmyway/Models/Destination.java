package com.example.onmyway.Models;

import com.google.android.gms.maps.model.LatLng;

public class Destination {

    private LatLng latLng;
    private String destination;

    public Destination() {

    }

    public Destination(LatLng latLng, String destination) {
        this.latLng = latLng;
        this.destination = destination;
    }


    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}

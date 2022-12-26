package com.logoutbye.garbagemanagementapp.utils;

import java.io.Serializable;

public class RoutPointClass implements Serializable {
    double originLat,originLong,destinationLat,destinationLong;
    String stringMessage="No Deatils";

    public RoutPointClass() {
    }

    public RoutPointClass(double originLat, double originLong, double destinationLat, double destinationLong,String stringMessage) {
        this.originLat = originLat;
        this.originLong = originLong;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.stringMessage = stringMessage;
    }

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLong() {
        return originLong;
    }

    public void setOriginLong(double originLong) {
        this.originLong = originLong;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(double destinationLong) {
        this.destinationLong = destinationLong;
    }

    public String getStringMessage() {
        return stringMessage;
    }

    public void setStringMessage(String stringMessage) {
        this.stringMessage = stringMessage;
    }
}

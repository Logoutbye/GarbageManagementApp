package com.logoutbye.garbagemanagementapp.utils;

import java.io.Serializable;

public class BinPoint implements Serializable {

    double lati,longi;

    public BinPoint() {
    }

    public BinPoint(double lati, double longi) {
        this.lati = lati;
        this.longi = longi;
    }



    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }




}

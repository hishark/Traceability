package com.ecnu.traceability.judge;

import org.greenrobot.greendao.annotation.Entity;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class GPSRisk {
    public double latitude;
    public double longitude;
    public Date date;

    public GPSRisk(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Generated(hash = 1433741067)
    public GPSRisk(double latitude, double longitude, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    @Generated(hash = 1191276925)
    public GPSRisk() {
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

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

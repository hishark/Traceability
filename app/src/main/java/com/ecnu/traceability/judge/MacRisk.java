package com.ecnu.traceability.judge;

import org.greenrobot.greendao.annotation.Entity;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

@Entity
public class MacRisk {
    public String macAddress;
    public Date date;

    @Generated(hash = 1496003659)
    public MacRisk(String macAddress, Date date) {
        this.macAddress = macAddress;
        this.date = date;
    }

    @Generated(hash = 1157200513)
    public MacRisk() {
    }


    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

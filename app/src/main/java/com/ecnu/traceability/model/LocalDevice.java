package com.ecnu.traceability.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LocalDevice {
    @Id(autoincrement = true)
    private Long id;
    private String mac;
    private String deviceId;

    public LocalDevice(String mac, String deviceId) {
        this.mac = mac;
        this.deviceId = deviceId;
    }

    @Generated(hash = 2056374487)
    public LocalDevice(Long id, String mac, String deviceId) {
        this.id = id;
        this.mac = mac;
        this.deviceId = deviceId;
    }

    @Generated(hash = 1563285023)
    public LocalDevice() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

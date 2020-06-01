package com.ecnu.traceability.bluetooth.Dao;

import android.bluetooth.BluetoothDevice;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class BluetoothDeviceEntity {
    @Id(autoincrement = true)
    private Long id;
    private short signalStrength;
    private String macAddress;
    private String deviceName;
    private java.util.Date date=new Date();
    @Transient
    private BluetoothDevice deviceInfo;


    public BluetoothDeviceEntity(short signalStrength, BluetoothDevice deviceInfo) {
        this.signalStrength = signalStrength;
        this.deviceInfo = deviceInfo;
        this.macAddress=deviceInfo.getAddress();
        this.deviceName=deviceInfo.getName();
    }



    @Generated(hash = 1142439237)
    public BluetoothDeviceEntity(Long id, short signalStrength, String macAddress,
            String deviceName, java.util.Date date) {
        this.id = id;
        this.signalStrength = signalStrength;
        this.macAddress = macAddress;
        this.deviceName = deviceName;
        this.date = date;
    }



    @Generated(hash = 514403774)
    public BluetoothDeviceEntity() {
    }

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public short getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(short signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public BluetoothDevice getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(BluetoothDevice deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

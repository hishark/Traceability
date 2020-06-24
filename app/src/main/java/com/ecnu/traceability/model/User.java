package com.ecnu.traceability.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class User {
    @Id(autoincrement = true)
    private Long id;
    private String macAddress;
    private String tel;
    private String address;

    public User(String macAddress, String tel, String address) {
        this.macAddress = macAddress;
        this.tel = tel;
        this.address = address;
    }

    @Generated(hash = 1996037426)
    public User(Long id, String macAddress, String tel, String address) {
        this.id = id;
        this.macAddress = macAddress;
        this.tel = tel;
        this.address = address;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

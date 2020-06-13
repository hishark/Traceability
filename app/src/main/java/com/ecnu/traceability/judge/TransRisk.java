package com.ecnu.traceability.judge;

import org.greenrobot.greendao.annotation.Entity;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;
@Entity
public class TransRisk {

    public String type;//类型：汽车、火车、飞机、公交
    public String NO;//车次
    public Integer seat;//座位号
    public Date date;//日期



    @Generated(hash = 1771478805)
    public TransRisk(String type, String NO, Integer seat, Date date) {
        this.type = type;
        this.NO = NO;
        this.seat = seat;
        this.date = date;
    }

    @Generated(hash = 367358733)
    public TransRisk() {
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNO() {
        return NO;
    }

    public void setNO(String NO) {
        this.NO = NO;
    }

    public Integer getSeat() {
        return seat;
    }

    public void setSeat(Integer seat) {
        this.seat = seat;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

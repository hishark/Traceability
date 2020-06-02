package com.ecnu.traceability.transportation.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class TransportationEntity {
    @Id(autoincrement = true)
    private Long id;
    private String type;//类型：汽车、火车、飞机、公交
    private String NO;//车次
    private Integer seat;//座位号
    private Date date;//日期

    public TransportationEntity(String type, String NO, Integer seat, Date date) {
        this.type = type;
        this.NO = NO;
        this.seat = seat;
        this.date = date;
    }

    @Generated(hash = 966474886)
    public TransportationEntity(Long id, String type, String NO, Integer seat,
            Date date) {
        this.id = id;
        this.type = type;
        this.NO = NO;
        this.seat = seat;
        this.date = date;
    }

    @Generated(hash = 1184358368)
    public TransportationEntity() {
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

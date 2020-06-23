package com.ecnu.traceability.machine_learning;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LearningData {
    @Id(autoincrement = true)
    private Long id;
    private Integer label;
    private Double signal_strength;
    private Double avg_mac_time;
    private Double trans_count;
    private Double avg_seat_differ;
    private Double gps_distance;
    private Double avg_gps_time;
    private Date date;

    public LearningData(Integer label, Double signal_strength, Double avg_mac_time, Double trans_count, Double avg_seat_differ, Double gps_distance, Double avg_gps_time, Date date) {
        this.label = label;
        this.signal_strength = signal_strength;
        this.avg_mac_time = avg_mac_time;
        this.trans_count = trans_count;
        this.avg_seat_differ = avg_seat_differ;
        this.gps_distance = gps_distance;
        this.avg_gps_time = avg_gps_time;
        this.date = date;
    }

    @Generated(hash = 101093735)
    public LearningData(Long id, Integer label, Double signal_strength, Double avg_mac_time, Double trans_count, Double avg_seat_differ, Double gps_distance, Double avg_gps_time,
            Date date) {
        this.id = id;
        this.label = label;
        this.signal_strength = signal_strength;
        this.avg_mac_time = avg_mac_time;
        this.trans_count = trans_count;
        this.avg_seat_differ = avg_seat_differ;
        this.gps_distance = gps_distance;
        this.avg_gps_time = avg_gps_time;
        this.date = date;
    }

    @Generated(hash = 835972493)
    public LearningData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public Double getSignal_strength() {
        return signal_strength;
    }

    public void setSignal_strength(Double signal_strength) {
        this.signal_strength = signal_strength;
    }

    public Double getAvg_mac_time() {
        return avg_mac_time;
    }

    public void setAvg_mac_time(Double avg_mac_time) {
        this.avg_mac_time = avg_mac_time;
    }

    public Double getTrans_count() {
        return trans_count;
    }

    public void setTrans_count(Double trans_count) {
        this.trans_count = trans_count;
    }

    public Double getAvg_seat_differ() {
        return avg_seat_differ;
    }

    public void setAvg_seat_differ(Double avg_seat_differ) {
        this.avg_seat_differ = avg_seat_differ;
    }

    public Double getGps_distance() {
        return gps_distance;
    }

    public void setGps_distance(Double gps_distance) {
        this.gps_distance = gps_distance;
    }

    public Double getAvg_gps_time() {
        return avg_gps_time;
    }

    public void setAvg_gps_time(Double avg_gps_time) {
        this.avg_gps_time = avg_gps_time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

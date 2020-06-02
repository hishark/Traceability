package com.ecnu.traceability.information_reporting.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ReportInfoEntity {

    @Id(autoincrement = true)
    private Long id;
    private String text;
    private java.util.Date Date;

    public ReportInfoEntity(String text, java.util.Date date) {
        this.text = text;
        Date = date;
    }

    @Generated(hash = 526189997)
    public ReportInfoEntity(Long id, String text, java.util.Date Date) {
        this.id = id;
        this.text = text;
        this.Date = Date;
    }

    @Generated(hash = 1020024505)
    public ReportInfoEntity() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public java.util.Date getDate() {
        return Date;
    }

    public void setDate(java.util.Date date) {
        Date = date;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

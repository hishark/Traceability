package com.ecnu.traceability.information_reporting.Dao;

import com.ecnu.traceability.Utils.StringDateConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ReportInfoEntity {

    @Id(autoincrement = true)
    private Long id;
    private String text;
    @Convert(converter = StringDateConverter.class, columnType = String.class)
    private java.util.Date date;

    public ReportInfoEntity(String text, java.util.Date date) {
        this.text = text;
        this.date = date;
    }

    @Generated(hash = 785034961)
    public ReportInfoEntity(Long id, String text, java.util.Date date) {
        this.id = id;
        this.text = text;
        this.date = date;
    }

    @Generated(hash = 1020024505)
    public ReportInfoEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

package com.ecnu.traceability.ePayment.Dao;

//import com.ecnu.traceability.Utils.StringDateConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class EPaymentEntity {
    @Id(autoincrement = true)
    private Long id;
    private Double latitude;
    private Double longitude;
//    @Convert(converter = StringDateConverter.class, columnType = String.class)
    private Date date;

    public EPaymentEntity(Double latitude, Double longitude, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    @Generated(hash = 407634282)
    public EPaymentEntity(Long id, Double latitude, Double longitude, Date date) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    @Generated(hash = 1502616631)
    public EPaymentEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

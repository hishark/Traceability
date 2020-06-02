package com.ecnu.traceability.ePayment;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.ePayment.Dao.EPaymentEntity;
import com.ecnu.traceability.location.Dao.LocationEntity;

import java.util.Date;

public class EPayment extends Service {
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.init(this);
        addEPaymentInfo();

    }

    //mock data
    public void addEPaymentInfo() {
        dbHelper.getSession().getEPaymentEntityDao().insert(new EPaymentEntity(31.764645, 121.38803, new Date()));
        dbHelper.getSession().getEPaymentEntityDao().insert(new EPaymentEntity(31.75888, 121.392321, new Date()));
        dbHelper.getSession().getEPaymentEntityDao().insert(new EPaymentEntity(31.754886, 121.39276, new Date()));
        dbHelper.getSession().getEPaymentEntityDao().insert(new EPaymentEntity(31.752943, 121.3929, new Date()));
        dbHelper.getSession().getEPaymentEntityDao().insert(new EPaymentEntity(31.743058, 121.393904, new Date()));
        //直接整合进入LocationEntityDao
        dbHelper.getSession().getLocationEntityDao().insert(new LocationEntity(31.764645, 121.38803, new Date()));
        dbHelper.getSession().getLocationEntityDao().insert(new LocationEntity(31.75888, 121.392321, new Date()));
        dbHelper.getSession().getLocationEntityDao().insert(new LocationEntity(31.754886, 121.39276, new Date()));
        dbHelper.getSession().getLocationEntityDao().insert(new LocationEntity(31.752943, 121.3929, new Date()));
        dbHelper.getSession().getLocationEntityDao().insert(new LocationEntity(31.743058, 121.393904, new Date()));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

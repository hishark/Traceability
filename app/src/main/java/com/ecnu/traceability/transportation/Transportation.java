package com.ecnu.traceability.transportation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.util.Date;

/**
 * mock data
 */
public class Transportation extends Service {
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.init(this);
        addTransportationInfo();
    }

    public void addTransportationInfo() {
        TransportationEntity entity1 = new TransportationEntity("car", "abccdada11", 2, new Date());
        TransportationEntity entity2 = new TransportationEntity("bus", "abcca11", -1, new Date());
        TransportationEntity entity3 = new TransportationEntity("train", "5033", 101, new Date());
        TransportationEntity entity4 = new TransportationEntity("car", "abccdada11", 2, new Date());
        TransportationEntity entity5 = new TransportationEntity("car", "abccdada11", 2, new Date());
        dbHelper.getSession().getTransportationEntityDao().insert(entity1);
        dbHelper.getSession().getTransportationEntityDao().insert(entity2);
        dbHelper.getSession().getTransportationEntityDao().insert(entity3);
        dbHelper.getSession().getTransportationEntityDao().insert(entity4);
        dbHelper.getSession().getTransportationEntityDao().insert(entity5);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
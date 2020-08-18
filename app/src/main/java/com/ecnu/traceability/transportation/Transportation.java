package com.ecnu.traceability.transportation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.util.Date;

/**
 * mock data
 */
public class Transportation {
    private DBHelper dbHelper;

    public Transportation(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void addTransportationInfo() {
        TransportationEntity entity1 = new TransportationEntity("car", "苏EUK008", 2, new Date());
        TransportationEntity entity2 = new TransportationEntity("bus", "苏EUK722", -1, new Date());
        TransportationEntity entity3 = new TransportationEntity("train", "5033", 30, new Date());
        TransportationEntity entity4 = new TransportationEntity("car", "苏EUK003", 2, new Date());
        TransportationEntity entity5 = new TransportationEntity("car", "苏EUK001", 2, new Date());
        dbHelper.getSession().getTransportationEntityDao().insert(entity1);
        dbHelper.getSession().getTransportationEntityDao().insert(entity2);
        dbHelper.getSession().getTransportationEntityDao().insert(entity3);
        dbHelper.getSession().getTransportationEntityDao().insert(entity4);
        dbHelper.getSession().getTransportationEntityDao().insert(entity5);
    }


}

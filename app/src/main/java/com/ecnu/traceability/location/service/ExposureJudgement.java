package com.ecnu.traceability.location.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.DateUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ExposureJudgement extends Service {
    public static final int DIS_THRESHOLD = 10;//不开平方距离阈值
    public static final String TAG = "ExposureJudgement";
    private DBHelper dbHelper = DBHelper.getInstance();
    private static final  double EARTH_RADIUS = 6378137;//赤道半径



    private class PointDistance {
        public LocationEntity entity1;
        public LocationEntity entity2;
        public double distance;
        public boolean judgeFlag = false;


        public PointDistance(LocationEntity entity1, LocationEntity entity2, double distance) {
            this.entity1 = entity1;
            this.entity2 = entity2;
            this.distance = distance;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.init(this);
        int count=judge();
        Log.e(TAG, String.valueOf(count));
        Log.e(TAG, String.valueOf(count));
        Log.e(TAG, String.valueOf(count));
        Log.e(TAG, String.valueOf(count));
        Log.e(TAG, String.valueOf(count));
    }

    public List<LocationEntity> getDataFromServer() {


//        String url="";//网址加mac地址
//        HTTPUtils.getDataFromServer("", new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//            }
//        });


        //请求服务器的GPS数据
        // mock data
        //        121.38803,31.764645
        //        121.389532,31.76231
        //        121.392321,31.75888
        //        121.39276,31.754886
        //        121.3929,31.752943
        //        121.393904,31.743058

        List<LocationEntity> list=new ArrayList<LocationEntity>();
        list.add(new LocationEntity(31.764645,121.38803,new Date()));
        list.add(new LocationEntity(31.76231,121.389532,new Date()));
        list.add(new LocationEntity(31.75888,121.392321,new Date()));
        list.add(new LocationEntity(31.754886,121.39276,new Date()));
        list.add(new LocationEntity(31.752943,121.3929,new Date()));
        list.add(new LocationEntity(31.743058,121.393904,new Date()));

        return list;
    }

    public List<LocationEntity> getDataFromDatabase() {
        //mock data
        //        121.390463,31.76409
        //        121.393553,31.764473
        //        121.396975,31.76482
        //        121.39763,31.765103
        List<LocationEntity> locationList=new ArrayList<LocationEntity>();
        locationList.add(new LocationEntity(31.764645,121.38803,new Date()));
        locationList.add(new LocationEntity(31.76409,121.390463,new Date()));
        locationList.add(new LocationEntity(31.764473,121.393553,new Date()));
        locationList.add(new LocationEntity(31.76482,121.396975,new Date()));
        locationList.add(new LocationEntity(31.765103,121.39763,new Date()));


//        List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
        return locationList;
    }

    private static double rad(double d){
        return d * Math.PI / 180.0;
    }
    //地球上的经纬度距离计算
    public static double calDistance(LocationEntity point1, LocationEntity point2) {
        double radLat1 = rad(point1.getLatitude());
        double radLat2 = rad(point2.getLatitude());
        double a = radLat1 - radLat2;
        double b = rad(point1.getLongitude()) - rad(point2.getLongitude());
        double s = 2 *Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        return s;//单位米
    }
//    需要精心设计阈值
//    public double calDistance(LocationEntity point1, LocationEntity point2) {
//        //不开平方距离
//        double dis = Math.pow(point1.getLongitude() - point2.getLongitude(), 2) + Math.pow(point1.getLatitude() - point2.getLatitude(), 2);
//        return dis;
//    }

    public int dataJudge(List<PointDistance> list) {
        int count = 0;
        for (PointDistance pdc : list) {
            if (DateUtils.dataDiff(pdc.entity1.getDate(), pdc.entity2.getDate()) < 10) {
                pdc.judgeFlag = true;
                count++;
            }
        }

        return count;
    }

    public int judge() {
        List<LocationEntity> serverData = getDataFromServer();
        List<LocationEntity> localData = getDataFromDatabase();
        List<PointDistance> disList = new ArrayList<PointDistance>();
        //计算距离
        for (LocationEntity serData : serverData) {
            for (LocationEntity locData : localData) {
                double distance = calDistance(serData, locData);
                if (distance <= DIS_THRESHOLD) {
                    disList.add(new PointDistance(serData, locData, distance));
                }
            }
        }
        //判断时间段是否相同
        int count = dataJudge(disList);
        return count;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

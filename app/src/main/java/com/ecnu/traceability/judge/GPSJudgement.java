package com.ecnu.traceability.judge;

import android.os.Bundle;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.DateUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.model.LatLonPoint;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class GPSJudgement {
    public static final int DIS_THRESHOLD = 50;//距离阈值设置为50米
    public static final int TIME_THRESHOLD = 20;//时间阈值设置为10秒
    public static final String TAG = "GPSJudge";
    private DBHelper dbHelper = null;
    private static final double EARTH_RADIUS = 6378137;//赤道半径

    //联邦学习参数
    private double avgDistance = 0.0;
    private double gpsTime = 0.0;

    // 本地数据库的LocationEntity列表
    private List<LocationEntity> locationEntityList;
    // 来自于服务器端的病人的LocationEntity
    // private List<LocationEntity> serverDataList;

    public GPSJudgement(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

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

    public List<LocationEntity> getMockDataFromServer(String patientAddress) {
        //请求服务器的GPS数据
        // mock data
        //        121.38803,31.764645
        //        121.389532,31.76231
        //        121.392321,31.75888
        //        121.39276,31.754886
        //        121.3929,31.752943
        //        121.393904,31.743058
        // 从服务器得到一个患者的位置数据
        List<LocationEntity> list = new ArrayList<LocationEntity>();
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));
        list.add(new LocationEntity(27.624571, 113.855194, new Date()));

        return list;
    }

    // 从本地数据库得到数据
    public List<LocationEntity> getDataFromDatabase() {
        locationEntityList = dbHelper.getSession().getLocationEntityDao().loadAll();
        //mock data
        //        List<LocationEntity> locationList = new ArrayList<LocationEntity>();
        //        locationList.add(new LocationEntity(31.764645, 121.38803, new Date()));
        //        locationList.add(new LocationEntity(31.76409, 121.390463, new Date()));
        //        locationList.add(new LocationEntity(31.764473, 121.393553, new Date()));
        //        locationList.add(new LocationEntity(31.76482, 121.396975, new Date()));
        //        locationList.add(new LocationEntity(31.765103, 121.39763, new Date()));
        //        List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
        return locationEntityList;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    //地球上的经纬度距离计算
    public static double calDistance(LocationEntity point1, LocationEntity point2) {
        double radLat1 = rad(point1.getLatitude());
        double radLat2 = rad(point2.getLatitude());
        double a = radLat1 - radLat2;
        double b = rad(point1.getLongitude()) - rad(point2.getLongitude());
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s;//单位米
    }

    //判断相同地点是否在相同时间段内发生的
    public List<LocationEntity> dateJudge(List<PointDistance> list) {

//        avgDistance = 0;
//        gpsTime = 0;

        List<LocationEntity> timeLocationList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        for (PointDistance pdc : list) {
            if (DateUtils.dataDiff(pdc.entity1.getDate(), pdc.entity2.getDate()) < TIME_THRESHOLD) {
                pdc.judgeFlag = true;
                // 把本地的日期添加到dateList
                timeLocationList.add(pdc.entity2);
                addRiskToDB(pdc.entity2);//将与病人GPS位置接触的风险信息保存到数据库持久化
                avgDistance += pdc.distance;
            }
        }
        avgDistance = avgDistance / (timeLocationList.size() == 0 ? 1 : timeLocationList.size() + 1);
        gpsTime = timeLocationList.size() * 10 * 0.0001;

        return timeLocationList;
    }

    public void addRiskToDB(LocationEntity entity) {
        dbHelper.getSession().getGPSRiskDao().insert(new GPSRisk(entity.getLatitude(), entity.getLongitude(), entity.getDate()));

    }

    public List<PointDistance> dateJudge(List<LocationEntity> serverDataList, List<LocationEntity> localData) {
        List<PointDistance> timeList = new ArrayList<PointDistance>();
        for (LocationEntity serData : serverDataList) {
            for (LocationEntity locData : localData) {
                if (DateUtils.dataDiff(serData.getDate(), locData.getDate()) < TIME_THRESHOLD) {
                    timeList.add(new PointDistance(serData, locData, 0));
                }
            }
        }
        return timeList;
    }

    public List<PointDistance> distanceJudge(List<PointDistance> dateList) {
        List<PointDistance> list = new ArrayList<>();
        for (PointDistance pd : dateList) {
            pd.distance = calDistance(pd.entity1, pd.entity2);
            if (pd.distance <= DIS_THRESHOLD) {
                list.add(pd);
            }
        }
        return list;
    }

    public Bundle judge(List<LocationEntity> serverDataList, List<LocationEntity> localData) {
        List<PointDistance> dateList = dateJudge(serverDataList, localData);
        List<PointDistance> disList = distanceJudge(dateList);
        List<LocationEntity> list = new ArrayList<>();
        for (PointDistance data : disList) {
            list.add(data.entity1);
            avgDistance += data.distance;
        }
        avgDistance = avgDistance / (disList.size() == 0 ? 1 : disList.size() + 1);
        gpsTime = disList.size() * 10 * 0.0001;

        //判断时间段是否相同
        Bundle bundle = new Bundle();
        bundle.putSerializable("gpsJudge", (Serializable) list);
        bundle.putDouble("avgDistance", avgDistance);
        bundle.putDouble("gpsTime", gpsTime);
        return bundle;

    }
    //计算患者与用户之间GPS定位位置相同并且时间相同的数量（该数量与时间有关定位每10s一次，有多少次就有多少个10s的接触）
    //    public Bundle judge(List<LocationEntity> serverDataList, List<LocationEntity> localData) {
    //        List<PointDistance> disList = new ArrayList<PointDistance>();
    //        //计算距离
    //        for (LocationEntity serData : serverDataList) {
    //            Log.i(TAG, "judge");
    //            for (LocationEntity locData : localData) {
    //                double distance = calDistance(serData, locData);
    //                if (distance <= DIS_THRESHOLD) {
    //                    disList.add(new PointDistance(serData, locData, distance));
    //                }
    //            }
    //        }
    //        //判断时间段是否相同
    //        List<LocationEntity> timeLocationList = dateJudge(disList);
    //        Bundle bundle = new Bundle();
    //        bundle.putSerializable("gpsJudge", (Serializable) timeLocationList);
    //        bundle.putDouble("avgDistance",avgDistance);
    //        bundle.putDouble("gpsTime",gpsTime);
    //        //return dateList.size();
    //        Log.i(TAG, "judge: ========================");
    //        return bundle;
    //    }

    //解析服务端发送来的数据
    public List<LocationEntity> parseDate(Response response) {
        List<LocationEntity> serverDataList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        try {
            String body = response.body().string();
            Log.e("parseDate Location", body);
            JSONArray array = new JSONArray(body);

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                Double latitude = jsonObject.getDouble("latitude");
                Double longitude = jsonObject.getDouble("longitude");
                String date = jsonObject.getString("date");
                Date parsedDate = sdf.parse(date);
                serverDataList.add(new LocationEntity(latitude, longitude, parsedDate));

            }
        } catch (JSONException | ParseException | IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "parseDate++++++++++++++++++++");
        return serverDataList;
    }


    public void queryPatientLocationInfo(String patientAddress, Callback locationCallback) {
        HTTPUtils.queryPatientLocationInfo(patientAddress, locationCallback);
    }

}

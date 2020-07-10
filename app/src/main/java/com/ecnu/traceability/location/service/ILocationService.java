package com.ecnu.traceability.location.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amap.api.fence.GeoFence;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.ecnu.traceability.InfoToOneNet;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.model.LatLonPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ILocationService extends Service {

    private static final String TAG = "ILocationService";
    private DBHelper dbHelper = DBHelper.getInstance();
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private long count = 0;
    private InfoToOneNet oneNetSender;

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            //获取定位时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+08"));
            String datetime = sdf.format(new Date());
            Date date = new Date();
            try {
                date = sdf.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            LocationEntity entity = new LocationEntity(aMapLocation.getLatitude(), aMapLocation.getLongitude(), date);
            dbHelper.getSession().getLocationEntityDao().insert(entity);
            Log.e(TAG, "------------------------add to database---------------------");
            if (count % 5 == 0) {
                //查询当前的风险等级，如果风险等级为高，主动实时向OneNET
                SharedPreferences sharedPreferences = getSharedPreferences("fence_status", MODE_PRIVATE);
                int fenceStatus = sharedPreferences.getInt("FENCE_STATUS_", 4);
                Log.i(TAG, "地理围栏状态是：" + String.valueOf(fenceStatus));
                if (fenceStatus == GeoFence.STATUS_OUT) {
                    Log.i(TAG, "向oneNET发送位置信息");
                    //实时向oneNET发送位置
                    HTTPUtils.pushRealtimeLocation(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), date);
                    //oneNetSender.pushRealTimeLocation(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), date);
                }
            }
            count++;

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.init(this);
        startPreciseLocation();
        oneNetSender = new InfoToOneNet(dbHelper);
    }

    public void startPreciseLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(10000);
        if (null != mLocationClient) {
            mLocationClient.setLocationOption(mLocationOption);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
        mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
        Log.e(TAG, "------------------------onDestroy---------------------");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

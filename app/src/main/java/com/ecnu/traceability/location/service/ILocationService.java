package com.ecnu.traceability.location.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.location.Dao.LocationEntity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ILocationService extends Service implements GeocodeSearch.OnGeocodeSearchListener {

    private static final String TAG = "ILocationService";
    private DBHelper dbHelper = DBHelper.getInstance();
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    public GeocodeSearch geocoderSearch =null;

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            //获取定位时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(aMapLocation.getTime());
            LocationEntity entity=new LocationEntity(aMapLocation.getLatitude(),aMapLocation.getLongitude(),date);
            dbHelper.getSession().getLocationEntityDao().insert(entity);
            Log.e(TAG, "------------------------add to database---------------------");
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.init(this);
        startLogLocation();

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    public void startLogLocation() {
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
        mLocationOption.setInterval(5000);
        if(null != mLocationClient){
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

    /**
     * 执行这个方法进行逆地理坐标转换
     * @param point
     */
    public void latlonToLocation(LatLonPoint point){

        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(point, 200,GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        //解析result获取地址描述信息
        //逆地理信息
        regeocodeResult.getRegeocodeAddress();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        //解析result获取地址描述信息

    }
}

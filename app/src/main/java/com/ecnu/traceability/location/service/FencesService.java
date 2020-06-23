package com.ecnu.traceability.location.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;

import java.util.List;

public class FencesService extends Service {
    private String macAddress;
    private static final String TAG="FencesService";

    //实例化地理围栏客户端
    GeoFenceClient mGeoFenceClient = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        macAddress= OneNetDeviceUtils.macAddress;

        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);
        addFence();
    }

    public void addFence(){
        if (mGeoFenceClient != null) {
            mGeoFenceClient.removeGeoFence();
        } else {
            mGeoFenceClient = new GeoFenceClient(this);
        }
        //创建一个中心点坐标
        DPoint centerPoint = new DPoint();
        //设置中心点纬度
        centerPoint.setLatitude(39.123D);
        //设置中心点经度
        centerPoint.setLongitude(116.123D);
        //执行添加围栏的操作
        mGeoFenceClient.addGeoFence(centerPoint, 100f, "公司打卡");
        mGeoFenceClient.setGeoFenceListener(fenceListenter);
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_OUT | GeoFenceClient.GEOFENCE_STAYED);
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        //会清除所有围栏
        //mGeoFenceClient.removeGeoFence();
    }
    //创建回调监听
    GeoFenceListener fenceListenter = new GeoFenceListener() {
        @Override
        public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
            if(errorCode == GeoFence.ADDGEOFENCE_SUCCESS){//判断围栏是否创建成功
                Log.i(TAG, "添加围栏成功!!");
                //geoFenceList是已经添加的围栏列表，可据此查看创建的围栏
            } else {
                Log.i(TAG, "添加围栏失败!!");
            }
        }
    };

    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.ecnu.traceability.location.service";
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                //解析广播内容

                //获取Bundle
                Bundle bundle = intent.getExtras();
                //获取围栏行为：
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);//处理
                switch (status){
                    case GeoFence.STATUS_IN:
                        Log.i(TAG, "从外部进入");
                        break;
                    case GeoFence.STATUS_OUT:
                        Log.i(TAG, "从内部出去");
                        break;
                    case GeoFence.STATUS_STAYED:
                        Log.i(TAG, "在内部停留超过十分钟");
                        break;
                }

                //获取自定义的围栏标识：
                String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                //获取围栏ID:
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //获取当前有触发的围栏对象：
                GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
            }
        }
    };
}

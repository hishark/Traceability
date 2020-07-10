package com.ecnu.traceability.location.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

public class SchooleFenceActivity extends AppCompatActivity {
    private static final String TAG = "SchooleFenceActivity";
    private MapView mMapView = null;
    private AMap amap = null;
    //实例化地理围栏客户端
    GeoFenceClient mGeoFenceClient = null;
    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);
        setTitle("疫情期间校园管理");
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        boundsBuilder = new LatLngBounds.Builder();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (amap == null) {
            amap = mMapView.getMap();
        }

        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);

        addFence();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    private LatLngBounds getBounds(List<LatLng> pointlist) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (pointlist == null) {
            return b.build();
        }
        for (int i = 0; i < pointlist.size(); i++) {
            b.include(pointlist.get(i));
        }
        return b.build();

    }

    public void addFence() {
        if (mGeoFenceClient != null) {
            mGeoFenceClient.removeGeoFence();
        } else {
            mGeoFenceClient = new GeoFenceClient(this);
        }

        List<DPoint> points = new ArrayList<DPoint>();

        points.add(new DPoint(31.231483, 121.407145));
        points.add(new DPoint(31.231212, 121.407129));
        points.add(new DPoint(31.230015, 121.40781));
        points.add(new DPoint(31.23001, 121.407896));
        points.add(new DPoint(31.229405, 121.408223));
        points.add(new DPoint(31.229634, 121.408786));
        points.add(new DPoint(31.228974, 121.40907));
        points.add(new DPoint(31.229258, 121.409934));
        points.add(new DPoint(31.226203, 121.410733));
        points.add(new DPoint(31.226148, 121.410374));
        points.add(new DPoint(31.226084, 121.409639));
        points.add(new DPoint(31.225579, 121.407788));
        points.add(new DPoint(31.225377, 121.407107));
        points.add(new DPoint(31.225325, 121.407075));
        points.add(new DPoint(31.225146, 121.406555));
        points.add(new DPoint(31.223962, 121.406394));
        points.add(new DPoint(31.22393, 121.406158));
        points.add(new DPoint(31.223155, 121.406174));
        points.add(new DPoint(31.223155, 121.404565));
        points.add(new DPoint(31.222696, 121.404538));
        points.add(new DPoint(31.222678, 121.404651));
        points.add(new DPoint(31.222058, 121.404769));
        points.add(new DPoint(31.222045, 121.404082));
        points.add(new DPoint(31.221687, 121.404136));
        points.add(new DPoint(31.221714, 121.404141));
        points.add(new DPoint(31.221526, 121.402811));
        points.add(new DPoint(31.223999, 121.40272));
        points.add(new DPoint(31.225756, 121.40235));
        points.add(new DPoint(31.225926, 121.403197));
        points.add(new DPoint(31.226733, 121.402961));
        points.add(new DPoint(31.227077, 121.40302));
        points.add(new DPoint(31.227123, 121.40316));
        points.add(new DPoint(31.228531, 121.402334));
        points.add(new DPoint(31.230715, 121.40199));
        points.add(new DPoint(31.230903, 121.402505));
        points.add(new DPoint(31.230884, 121.402554));
        points.add(new DPoint(31.230948, 121.402543));
        points.add(new DPoint(31.2311, 121.403289));
        points.add(new DPoint(31.230673, 121.403471));
        points.add(new DPoint(31.231017, 121.404705));
        points.add(new DPoint(31.231852, 121.404619));
        points.add(new DPoint(31.231554, 121.406995));
        points.add(new DPoint(31.231483, 121.407145));


        drawPolygon(points);
        mGeoFenceClient.addGeoFence(points, "华东师范大学中山北路校区");
        mGeoFenceClient.setGeoFenceListener(fenceListenter);
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_OUT | GeoFenceClient.GEOFENCE_STAYED);
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);

        //会清除所有围栏
        //mGeoFenceClient.removeGeoFence();
    }

    private void drawPolygon(List<DPoint> pointList) {
        List<LatLng> lst = new ArrayList<LatLng>();
        for (DPoint dp : pointList) {
            lst.add(new LatLng(dp.getLatitude(), dp.getLongitude()));
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(lst);
        polylineOptions.zIndex(2);
        polylineOptions.color(Color.argb(255, 255, 20, 147));
        //绘制轨迹，移动地图显示
        if (lst != null && lst.size() > 0) {
            amap.addPolyline(polylineOptions);
            amap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(lst), 200));
        }

    }

    //创建回调监听
    GeoFenceListener fenceListenter = new GeoFenceListener() {
        @Override
        public void onGeoFenceCreateFinished(List<GeoFence> list, int errorCode, String s) {
            if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {//判断围栏是否创建成功
                Log.i(TAG, "添加围栏成功!!");
                //geoFenceList是已经添加的围栏列表，可据此查看创建的围栏
            } else {
                Log.i(TAG, "添加围栏失败!!");
            }
        }
    };

    //定义接收广播的action字符串
    public static final String GEOFENCE_BROADCAST_ACTION = "com.ecnu.traceability.location.service.school";
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                //解析广播内容

                //获取Bundle
                Bundle bundle = intent.getExtras();
                //获取围栏行为：
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);//处理

                SharedPreferences.Editor editor = getSharedPreferences("fence_status", MODE_PRIVATE).edit();

                switch (status) {
                    case GeoFence.STATUS_IN:
                        Log.i(TAG, "从外部进入");
                        editor.putInt("FENCE_STATUS_", GeoFence.STATUS_IN);
                        editor.apply();
                        break;
                    case GeoFence.STATUS_OUT:
                        Log.i(TAG, "从内部出去");
                        editor.putInt("FENCE_STATUS_", GeoFence.STATUS_OUT);
                        editor.apply();
                        NotificationUtil.notification(getApplicationContext(), "走出隔离区警告", "你已经走出隔离区，将对您的活动轨迹实时监控", 2);

                        break;
                    case GeoFence.STATUS_STAYED:
                        Log.i(TAG, "在内部停留超过十分钟");
                        editor.putInt("FENCE_STATUS_", GeoFence.STATUS_STAYED);
                        editor.apply();
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

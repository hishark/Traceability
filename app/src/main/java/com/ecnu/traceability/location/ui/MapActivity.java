package com.ecnu.traceability.location.ui;

import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.PathSmoothTool;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    public static final String TAG = "MapActivity";

    private MapView mMapView = null;
    private AMap amap = null;
    private List<LatLng> mOriginList = new ArrayList<LatLng>();
    private Polyline mOriginPolyline, mkalmanPolyline;
    private CheckBox mOriginbtn, mkalmanbtn;
    private PathSmoothTool mpathSmoothTool;
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper.init(this);
        setContentView(R.layout.activity_mapactivity);
        setTitle("活动轨迹展示");
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (amap == null) {
            amap = mMapView.getMap();
        }
        addTrajectory();
    }

    public void addTrajectory() {
        List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
        locationList.remove(0);//第一个数据往往不正确，直接删除
        List<LatLng> mOriginList = new ArrayList<LatLng>();

        //        Log.e(TAG, String.valueOf(locationList.size()));
        //        mOriginList.addAll(kmeans(locationList));

        int count = 0;
        double x = 0;
        double y = 0;
        int k = 5;
        for (LocationEntity latlon : locationList) {
            count++;
            x += latlon.getLatitude();
            y += latlon.getLongitude();
            if (count % k == 0) {
                mOriginList.add(new LatLng(x / k, y / k));
                //                Log.e("计算结果",x / k+"  "+y/k);
                x = 0;
                y = 0;
            }
        //            Log.e(TAG,latlon.getLatitude()+"/"+latlon.getLongitude());
        }
        //        for (LatLng lo:mOriginList){
        //            Log.e("data",lo.latitude+" "+lo.longitude);
        //        }
        // 获取轨迹坐标点
        PathSmoothTool mpathSmoothTool = new PathSmoothTool();
        //设置平滑处理的等级
        mpathSmoothTool.setIntensity(4);
        List<LatLng> pathoptimizeList = mpathSmoothTool.pathOptimize(mOriginList);
        //绘制轨迹，移动地图显示
        if (mOriginList != null && mOriginList.size() > 0) {
            mOriginPolyline = amap.addPolyline(new PolylineOptions().addAll(pathoptimizeList).color(Color.BLUE));
            amap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(pathoptimizeList), 200));
        }
    }

    private List<LatLng> kmeans(List<LocationEntity> locationList) {
//        return centerList;
        return null;
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
//
//    //根据配速获取不同的色值。
//    private int getColorFromSpeed(int pace) {
//        int paceMin = pace / TimeDateUtil.TIME_MIN_INT;
//        switch (paceMin) {
//            case 12:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_1);
//            case 11:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_2);
//            case 10:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_3);
//            case 9:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_4);
//            case 8:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_5);
//            case 7:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_6);
//            case 6:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_7);
//            case 5:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_8);
//            case 4:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_9);
//            case 3:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_10);
//            case 2:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_11);
//            case 1:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_12);
//            default:
//                return ColorUtil.getResourcesColor(R.color.location_trace_pace_def);
//        }
//    }


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
}

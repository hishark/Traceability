package com.ecnu.traceability.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.ePayment.Dao.EPaymentEntity;
import java.util.List;

public class EPaymentShow extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener {

    private MapView mMapView = null;
    private AMap amap = null;
    private DBHelper dbHelper = DBHelper.getInstance();
    public GeocodeSearch geocoderSearch = null;
    private Marker pubMaker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_payment_show);

        setTitle("支付位置展示");
        dbHelper.init(this);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.e_pay_map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (amap == null) {
            amap = mMapView.getMap();
        }
        addLocationToMap();
        geocoderSearch = new GeocodeSearch(EPaymentShow.this);
        geocoderSearch.setOnGeocodeSearchListener(this);

        // 绑定 Marker 被点击事件
        amap.setOnMarkerClickListener(markerClickListener);
    }

    // 定义 Marker 点击事件监听
    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(Marker marker) {
            pubMaker = marker;

            LatLng point = marker.getPosition();
            LatLonPoint latLonPoint = new LatLonPoint(point.latitude, point.longitude);
            // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);//异步方法


            return false;
        }
    };

    public void addLocationToMap() {
        //        |position     |在地图上标记位置的经纬度值必填参数
        //        |title        |点标记的标题
        //        |snippet      |点标记的内容
        //        |draggable    |点标记是否可拖拽
        //        |visible      |点标记是否可见
        //        |anchor       |点标记的锚点
        //        |alpha        |点的透明度
        List<EPaymentEntity> list = dbHelper.getSession().getEPaymentEntityDao().queryBuilder().list();
        LatLng latLng=new LatLng(31.230532,121.449298);
        double latVal=0;
        double lonVal=0;
        int count=0;
        for (EPaymentEntity entity : list) {
            latVal+=entity.getLatitude();
            lonVal+=entity.getLongitude();
            count++;
            LatLng point = new LatLng(entity.getLatitude(), entity.getLongitude());
            final Marker marker = amap.addMarker(new MarkerOptions().position(point).title("位置").
                    snippet("再次点击查看位置").icon(BitmapDescriptorFactory.fromView(this.getLayoutInflater().inflate(R.layout.marker_icon,null))));
        }
        if(latVal>0){
            latLng=new LatLng(latVal/count,lonVal/count);
        }
        amap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.e("LocationAnalysis", regeocodeResult.getRegeocodeAddress().getFormatAddress());
        pubMaker.setSnippet(regeocodeResult.getRegeocodeAddress().getFormatAddress());
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

}
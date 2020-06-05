package com.ecnu.traceability.data_analyze;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationAnalysis extends Service implements GeocodeSearch.OnGeocodeSearchListener {
    private static final int DATA_UNIT = 12960;
    private DBHelper dbHelper = DBHelper.getInstance();

    private int count = 0;
    private int index = 0;
    public GeocodeSearch geocoderSearch = null;
    public Map<String, Integer> locationMap = null;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.init(this);

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);

    }

//    ----------------------------------通信相关--------------------------------------------
    private static final int MSG_ID_CLIENT = 1;
    private static final int MSG_ID_SERVER = 2;
    private static final String MSG_CONTENT = "getAddress";
    private Message mMessage = null;


    Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null && msg.arg1 == MSG_ID_CLIENT) {
                mMessage = msg;
                getDataFromClient();
                sendDataToClient(locationMap);
            }
        }
    });

    public void getDataFromClient() {
//        if (mMessage.getData() == null) {
//            return;
//        }
//        String content = (String) mMessage.getData().get(MSG_CONTENT);  //接收客户端的消息
//        Log.e("IPC", "Message from client: " + content);
        locationMap = new HashMap<String, Integer>();
//        processData();
    }

    public void sendDataToClient(Map<String, Integer> locationMap) {
        //回复消息给客户端
        Message replyMsg = Message.obtain();
        replyMsg.arg1 = MSG_ID_SERVER;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_CONTENT, "听到你的消息了");
//        bundle.putSerializable(MSG_CONTENT, (Serializable) locationMap);
        replyMsg.setData(bundle);

        try {
            mMessage.replyTo.send(replyMsg);     //回信
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
//    ------------------------------------------------------------------------------


    public int processData() {
        Long count = dbHelper.getSession().getLocationEntityDao().queryBuilder().count();
        int times = (int) Math.ceil(count * 1.0 / DATA_UNIT);
        for (int i = 0; i < times; i++) {
            List<LocationEntity> locList = getDataFromDatabase(i);
            double lat = 0;
            double lon = 0;
            for (LocationEntity locData : locList) {
                lat += locData.getLatitude();
                lon += locData.getLongitude();
            }
            LatLonPoint point = new LatLonPoint(lat / locList.size(), lon / locList.size());
            latlonToLocation(point);//逆地理编码
        }
        this.count = 0;
        this.index = 0;
        return times;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.e("LocationAnalysis", regeocodeResult.getRegeocodeAddress().getCity());
        updateLocationMap(regeocodeResult.getRegeocodeAddress().getCity());

        if (index == count) {
            Object[][] data = processRawData();
            sendDataToClient(locationMap);
//            showChart(data);
        }
    }

    public void updateLocationMap(String city) {
        if (locationMap.containsKey(city)) {
            int value = locationMap.get(city);
            locationMap.put(city, ++value);
        } else {
            locationMap.put(city, 1);
        }
    }

    public Object[][] processRawData() {

        Object[][] data = new Object[locationMap.size()][2];
        int index = 0;
        for (Map.Entry<String, Integer> entry : locationMap.entrySet()) {
            data[index][0] = entry.getKey();
            data[index][1] = entry.getValue();
        }
        return data;
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 执行这个方法进行逆地理坐标转换
     *
     * @param point
     */
    public void latlonToLocation(LatLonPoint point) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);//异步方法
    }

    public List<LocationEntity> getDataFromDatabase(int page) {

        List<LocationEntity> locList = dbHelper.getSession().getLocationEntityDao().queryBuilder()
                .offset(page * DATA_UNIT).limit(DATA_UNIT).orderAsc(LocationEntityDao.Properties.Date).list();
        return locList;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();
    }
}

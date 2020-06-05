package com.ecnu.traceability.data_analyze;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartView;
import com.ecnu.traceability.AAChartCoreLib.AAChartEnum.AAChartType;
import com.ecnu.traceability.AAChartCoreLib.AAOptionsModel.AADataLabels;
import com.ecnu.traceability.AAChartCoreLib.AAOptionsModel.AAPie;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationAnalysisActivity extends AppCompatActivity {
    private static final int DATA_UNIT = 12960;
    public GeocodeSearch geocoderSearch = null;
    public Map<String, Integer> locationMap = null;
    private DBHelper dbHelper = DBHelper.getInstance();
    private AAChartView aaChartView = null;
    private int count = 0;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_analysis);
        aaChartView = findViewById(R.id.AAChartView_location);
        dbHelper.init(this);

        locationMap = new HashMap<String, Integer>();

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                Log.e("LocationAnalysis", regeocodeResult.getRegeocodeAddress().getCity());
                updateLocationMap(regeocodeResult.getRegeocodeAddress().getCity());

                if (index == count) {
                    Object[][] data = processRawData();
                    showChart(data);
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });

        int count = processData();



        Intent intent = new Intent(this, LocationAnalysis.class);
        bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);
    }


    private static final int MSG_ID_CLIENT = 1;
    private static final int MSG_ID_SERVER = 2;
    private static final String MSG_CONTENT = "getAddress";
    /**
     * 客户端的 Messenger
     */
    Messenger mClientMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg != null && msg.arg1 == MSG_ID_SERVER) {
                if (msg.getData() == null) {
                    return;
                }

//                Map<String, Integer> locationMap= (Map<String, Integer>) msg.getData().get(MSG_CONTENT);
                String content = (String) msg.getData().get(MSG_CONTENT);
                Log.e("IPC", "Message from server: " + content);
            }
        }
    });

    //服务端的 Messenger
    private Messenger mServerMessenger;

    private ServiceConnection mMessengerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mServerMessenger = new Messenger(service);


            Message message = Message.obtain();
            message.arg1 = MSG_ID_CLIENT;
            Bundle bundle = new Bundle();
            bundle.putString(MSG_CONTENT, "测试信息");
            message.setData(bundle);
            message.replyTo = mClientMessenger;     //指定回信人是客户端定义的

            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mServerMessenger = null;
        }
    };


    public void showChart(Object[][] data) {
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Pie)
                .backgroundColor("#ffffff")
                .title("14天内到访地点")
                .subtitle("")
                .dataLabelsEnabled(true)//是否直接显示扇形图数据
                .yAxisTitle("℃")
                .series(new AAPie[]{
                        new AAPie()
                                .name("到访地点")
                                .innerSize("20%")
                                .size(150f)
                                .dataLabels(new AADataLabels()
                                        .enabled(true)
                                        .useHTML(true)
                                        .distance(5f)
                                        .format("<b>{point.name}</b>: <br> {point.percentage:.1f} %"))
                                .data(data),
                });
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel);
    }

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

    public List<LocationEntity> getDataFromDatabase(int page) {

        List<LocationEntity> locList = dbHelper.getSession().getLocationEntityDao().queryBuilder()
                .offset(page * DATA_UNIT).limit(DATA_UNIT).orderAsc(LocationEntityDao.Properties.Date).list();
        return locList;
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

}
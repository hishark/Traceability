package com.ecnu.traceability;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.IBluetoothService;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisActivity;
import com.ecnu.traceability.data_analyze.LocationAnalysisService;
import com.ecnu.traceability.data_analyze.LocationAnalysisActivity;
import com.ecnu.traceability.information_reporting.InformationReportingActivity;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.location.service.ILocationService;
import com.ecnu.traceability.location.ui.MapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 10;
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_show_trajectory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapActivity.class);
            }
        });
        findViewById(R.id.btn_update_info_to_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDateToServer();
            }
        });
        findViewById(R.id.btn_report_info).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(InformationReportingActivity.class);
            }
        });

        findViewById(R.id.btn_test_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(BluetoothAnalysisActivity.class);
            }
        });

        findViewById(R.id.btn_test_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(LocationAnalysisActivity.class);
            }
        });

        findViewById(R.id.btn_test_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        dbHelper.init(this);
        Intent bluetoothIntent = new Intent(this, IBluetoothService.class);
        Intent locationIntent = new Intent(this, ILocationService.class);
        startService(bluetoothIntent);
        startService(locationIntent);
        Log.e(TAG, "------------------------start---------------------");

        String mac = MacAddress.getBtAddressByReflection();
        if (mac != null) {
            Log.e(TAG, mac);
        } else {
            Toast.makeText(getApplicationContext(), "null!", Toast.LENGTH_LONG).show();
        }

//        OneNetDeviceUtils.addDevice();
//        Intent testIntent = new Intent(this, ExposureJudgement.class);
//        startService(testIntent);

        Intent intent = new Intent(this, LocationAnalysisService.class);
        bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);
    }

    public void sendDateToServer() {
        List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();

        String deviceId = "601016239";
        String datastream = "data_flow_1";
        JSONArray datapoints = new JSONArray();
        try {
            for (LocationEntity latlon : locationList) {
                JSONObject location = new JSONObject();

                location.putOpt("lat", latlon.getLatitude());
                location.putOpt("lon", latlon.getLongitude());
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", location);
                datapoints.put(datapoint);
            }

            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);
            OneNetDeviceUtils.sendData(deviceId, request);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSelfPermission();
    }

    private void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        showToast("请授予权限");
                        finish();
                        return;
                    }
                }
                break;
        }
    }


    //    ---------------------------------------------测试用-------------------------------------------------
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
                Map<String, Integer> locationMap = (Map<String, Integer>) msg.getData().get(MSG_CONTENT);
                Log.e("IPC", "Message from server: " + locationMap.size());
                onResciverData(locationMap);
            }
        }
    });

    //服务端的 Messenger
    private Messenger mServerMessenger;

    private ServiceConnection mMessengerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mServerMessenger = new Messenger(service);

        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mServerMessenger = null;
        }
    };

    public void onResciverData(Map<String, Integer> locationMap) {
        JSONArray datapoints = processRawData(locationMap);
        sendLocationDateToServer(datapoints);
    }

    public JSONArray processRawData(Map<String, Integer> locationMap) {
        JSONArray datapoints = new JSONArray();
        try {
            for (Map.Entry<String, Integer> entry : locationMap.entrySet()) {
                JSONObject location = new JSONObject();
                location.putOpt("value", entry.getValue());
                location.putOpt("name", entry.getKey());
                location.putOpt("color", getColor());
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", location);
                datapoints.put(datapoint);
            }

            return datapoints;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //随机生成颜色代码
    public String getColor() {
        //红色
        String red;
        //绿色
        String green;
        //蓝色
        String blue;
        //生成随机对象
        Random random = new Random();
        //生成红色颜色代码
        red = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成绿色颜色代码
        green = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成蓝色颜色代码
        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();

        //判断红色代码的位数
        red = red.length() == 1 ? "0" + red : red;
        //判断绿色代码的位数
        green = green.length() == 1 ? "0" + green : green;
        //判断蓝色代码的位数
        blue = blue.length() == 1 ? "0" + blue : blue;
        //生成十六进制颜色值
        String color = "#" + red + green + blue;
        return color;
    }


    public void sendLocationDateToServer(JSONArray datapoints) {

        String deviceId = "601016239";
        String datastream = "data_flow_3";

        try {
            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);
            OneNetDeviceUtils.sendData(deviceId, request);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }

    }
}
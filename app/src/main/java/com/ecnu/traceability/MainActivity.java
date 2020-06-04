package com.ecnu.traceability;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.LocationConverter;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.IBluetoothService;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisActivity;
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

        OneNetDeviceUtils.addDevice();
//        Intent testIntent = new Intent(this, ExposureJudgement.class);
//        startService(testIntent);
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

}
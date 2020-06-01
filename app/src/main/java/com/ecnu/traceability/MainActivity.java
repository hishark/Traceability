package com.ecnu.traceability;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.chinamobile.iot.onenet.http.Config;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.bluetooth.BaseActivity;
import com.ecnu.traceability.bluetooth.ConnectA2dpActivity;
import com.ecnu.traceability.bluetooth.service.IBluetoothService;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.location.service.ILocationService;
import com.ecnu.traceability.location.ui.MapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        findViewById(R.id.btn_connectA2dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(ConnectA2dpActivity.class);
                sendData();

            }
        });
        dbHelper.init(this);
        Intent bluetoothIntent = new Intent(this, IBluetoothService.class);
        Intent locationIntent = new Intent(this, ILocationService.class);
        //        Bundle bundle = new Bundle();
        //        bundle.putString("abc","test");
        //        intent.putExtras(bundle);
        startService(bluetoothIntent);
        startService(locationIntent);
        Log.e(TAG, "------------------------start---------------------");

        String mac = MacAddress.getBtAddressByReflection();
        if(mac!=null) {
            Log.e(TAG, mac);
        }else{
            Toast.makeText(getApplicationContext(),"null!", Toast.LENGTH_LONG).show();
        }

    }

    private void sendData() {
//        String deviceId = mDeviceIdLayout.getEditText().getText().toString().trim();
//        String datastream = mDataStreamLayout.getEditText().getText().toString().trim();
        String deviceId = "598576209";
        String datastream = "data_flow_1";
        List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
//        if (TextUtils.isEmpty(deviceId)) {
//            mDeviceIdLayout.setError(getResources().getString(R.string.device_id));
//            mDeviceIdLayout.requestFocus();
//            return;
//        }
//        if (TextUtils.isEmpty(datastream)) {
//            mDataStreamLayout.setError(getResources().getString(R.string.datastream));
//            mDataStreamLayout.requestFocus();
//            return;
//        }
        try {


            JSONArray datapoints = new JSONArray();


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

            OneNetApi.addDataPoints(deviceId, request.toString(), new OneNetApiCallback() {
                @Override
                public void onSuccess(String response) {
//                    displayLog(response);
                    Log.e(TAG, response);
                    Log.e(TAG, "=============发送成功=============");
                    Log.e(TAG, "=============发送成功=============");
                    Log.e(TAG, "=============发送成功=============");
                    Log.e(TAG, "=============发送成功=============");
                    Log.e(TAG, "=============发送成功=============");

                }

                @Override
                public void onFailed(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "=============发送失败=============");
                    Log.e(TAG, "=============发送失败=============");
                    Log.e(TAG, "=============发送失败=============");
                    Log.e(TAG, "=============发送失败=============");

                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "=============发送失败=============");
            Log.e(TAG, "=============发送失败=============");
            Log.e(TAG, "=============发送失败=============");
            Log.e(TAG, "=============发送失败=============");

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
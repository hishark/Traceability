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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.IBluetoothService;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisActivity;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisUtil;
import com.ecnu.traceability.data_analyze.LocationAnalysisActivity;
import com.ecnu.traceability.data_analyze.LocationAnalysisService;
import com.ecnu.traceability.data_analyze.RiskReportingService;
import com.ecnu.traceability.ePayment.EPayment;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntityDao;
import com.ecnu.traceability.information_reporting.InformationReportingActivity;
import com.ecnu.traceability.judge.JudgeActivity;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.location.service.ILocationService;
import com.ecnu.traceability.location.ui.MapActivity;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntityDao;
import com.ecnu.traceability.transportation.Transportation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.amap.api.maps.model.BitmapDescriptorFactory.getContext;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 10;
    private DBHelper dbHelper = DBHelper.getInstance();
    private InfoToOneNet oneNetDataSender = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("疫情追踪");
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_show_trajectory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapActivity.class);
            }
        });
//        findViewById(R.id.btn_update_info_to_server).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
////                oneNetDataSender.pushMapDateToOneNet(locationList);
//            }
//        });
//        findViewById(R.id.btn_report_info).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                startActivity(InformationReportingActivity.class);
//            }
//        });

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
                startActivity(InformationReportingActivity.class);
            }
        });
//        //将接触人数统计内容发送到OneNet
//        findViewById(R.id.btn_test_4).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                BluetoothAnalysisUtil util = new BluetoothAnalysisUtil(dbHelper);
//                Bundle bundle = util.processData();
//                Integer[] ans = (Integer[]) bundle.get("countMap");
//                ArrayList dateList = (ArrayList) bundle.get("dateList");
//
//                for (int i = 0; i < dateList.size(); i++) {
//                    Log.e("数据：", String.valueOf(ans[i]));
//                    Log.e("日期数据：", (String) dateList.get(i));
//                }
//            }
//        });
        //将主动上报信息传输到OneNet平台
//        findViewById(R.id.info_reporting_btn_one_net).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                List<ReportInfoEntity> reportInfoList = dbHelper.getSession().getReportInfoEntityDao().queryBuilder()
////                        .orderAsc(ReportInfoEntityDao.Properties.Date).list();
////                oneNetDataSender.pushReportAndpersonCountData(reportInfoList);
//            }
//        });

        findViewById(R.id.btn_riskLevel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(JudgeActivity.class);
            }
        });
        dbHelper.init(this);

        //蓝牙服务
        Intent bluetoothIntent = new Intent(this, IBluetoothService.class);
        //定位服务
        Intent locationIntent = new Intent(this, ILocationService.class);
        startService(bluetoothIntent);
        startService(locationIntent);

        Intent riskIntent = new Intent(this, RiskReportingService.class);
        startService(riskIntent);

        Log.e(TAG, "------------------------service start---------------------");

        OneNetDeviceUtils.getDevices(getContext(), dbHelper);//该设备如果没有注册，则注册到OneNet平台


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
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION);
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
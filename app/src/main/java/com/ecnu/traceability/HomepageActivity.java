package com.ecnu.traceability;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;

import com.amap.api.fence.GeoFence;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisActivity;
import com.ecnu.traceability.data_analyze.LocationAnalysisActivity;
import com.ecnu.traceability.ePayment.EPayment;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntityDao;
import com.ecnu.traceability.judge.JudgeActivity;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.location.service.FencesService;
import com.ecnu.traceability.location.ui.MapActivity;

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
import com.ecnu.traceability.data_analyze.BluetoothAnalysisActivity;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisUtil;
import com.ecnu.traceability.data_analyze.LocationAnalysisActivity;
import com.ecnu.traceability.data_analyze.LocationAnalysisService;
import com.ecnu.traceability.data_analyze.RiskReportingService;
import com.ecnu.traceability.information_reporting.InformationReportingActivity;
import com.ecnu.traceability.judge.JudgeActivity;
import com.ecnu.traceability.location.service.ILocationService;
import com.ecnu.traceability.location.ui.MapActivity;
import com.ecnu.traceability.model.User;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntityDao;
import com.ecnu.traceability.transportation.Transportation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.amap.api.maps.model.BitmapDescriptorFactory.getContext;

public class HomepageActivity extends BaseActivity {
    private static final String TAG = "HomepageActivity";
    private static final int REQUEST_PERMISSION = 10;
    private InfoToOneNet oneNetDataSender = null;

    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        setTitle("疫情追踪");

        findViewById(R.id.cardview_track_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapActivity.class);
            }
        });

        findViewById(R.id.cardview_contact_person).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(BluetoothAnalysisActivity.class);
            }
        });

        findViewById(R.id.cardview_location_analysis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LocationAnalysisActivity.class);
            }
        });

        findViewById(R.id.cardview_risk_level).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(JudgeActivity.class);
            }
        });


        verifyStoragePermission(HomepageActivity.this);//内存读取权限检查


        dbHelper.init(this);

        if (!isUserExist()) {
            startActivity(SignupActivity.class);
        }


        //蓝牙服务
        Intent bluetoothIntent = new Intent(this, IBluetoothService.class);
        //定位服务
        Intent locationIntent = new Intent(this, ILocationService.class);
        startService(bluetoothIntent);
        startService(locationIntent);

        Intent riskIntent = new Intent(this, RiskReportingService.class);
        startService(riskIntent);

        Log.e(TAG, "------------------------service start---------------------");

        OneNetDeviceUtils.initMacAddress(dbHelper);

        SharedPreferences.Editor editor = getSharedPreferences("fence_status", MODE_PRIVATE).edit();
        editor.putInt("FENCE_STATUS", 4);
        editor.apply();
        //Transportation transportation = new Transportation(dbHelper);
        //transportation.addTransportationInfo();
        //EPayment payment = new EPayment(dbHelper);
        //payment.addEPaymentInfo();

        // 上传数据相关
        oneNetDataSender = new InfoToOneNet(dbHelper);
        //判断服务是否运行
        boolean serviceFlag = GeneralUtils.isServiceRunning(getApplicationContext(), "com.ecnu.traceability.data_analyze.LocationAnalysisService");
        if (!serviceFlag) {
            Intent intent = new Intent(this, LocationAnalysisService.class);
            bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);
        }
    }


    public boolean isUserExist() {
        List<User> users = dbHelper.getSession().getUserDao().loadAll();
        if (null != users && users.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSelfPermission();
    }

    private void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(HomepageActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(HomepageActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(HomepageActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(HomepageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomepageActivity.this,
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homepage_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_upload) {
            startActivity(InformationReportingActivity.class);
        } else if (id == R.id.menu_warning) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomepageActivity.this);
            AlertDialog dialog = builder.setPositiveButton("是的，本人已确诊", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "信息上传中...", Toast.LENGTH_LONG).show();
                    upload();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            })
                    .setIcon(R.drawable.warning3)
                    .setMessage("您已经确诊为新冠肺炎了吗？")
                    .setTitle("紧急报告")
                    .create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    // read and write permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //获取存储权限
    public static void verifyStoragePermission(Activity activity) {
        // Get permission status
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission we request it
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //调用回调函数将信息发送至自己的服务器和OneNet服务器
    public void upload() {
        Transportation transInfo = new Transportation(dbHelper);
        EPayment paymentInfo = new EPayment(dbHelper);
        transInfo.addTransportationInfo();
        paymentInfo.addEPaymentInfo();

        Message message = Message.obtain();
        message.arg1 = MSG_ID_CLIENT;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_CONTENT, "准备发送信息到OneNet和自己的服务器");
        message.setData(bundle);
        message.replyTo = mClientMessenger;

        try {
            mServerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //    ---------------------------------------------Messager回调函数开始-------------------------------------------------
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

                //信息发送部分
                ////////////////////////////////////////////////////////向OneNet发送
                Map<String, Integer> locationMap = (Map<String, Integer>) msg.getData().get(MSG_CONTENT);
                oneNetDataSender.pushLocationMapData(locationMap);//向OneNet发送地点（地图）统计信息
                List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
                oneNetDataSender.pushMapDateToOneNet(locationList);//向OneNet地点统计（饼图）发送信息
                List<ReportInfoEntity> reportInfoList = dbHelper.getSession().getReportInfoEntityDao().queryBuilder()
                        .orderAsc(ReportInfoEntityDao.Properties.Date).list();
                oneNetDataSender.pushReportAndpersonCountData(reportInfoList);//向OneNet发送人数统计和主动上报的公告板信息（公告板和条形图）
                ////////////////////////////////////////////////////////向服务器发送
                List<TransportationEntity> transportationEntityList = dbHelper.getSession().getTransportationEntityDao().queryBuilder().orderAsc(TransportationEntityDao.Properties.Date).list();
                HTTPUtils.uploadInfoToServer(locationList, reportInfoList, transportationEntityList);//向自己的服务器发送信息（所有信息）
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
//    ---------------------------------------------Messager回调函数结束-------------------------------------------------
}

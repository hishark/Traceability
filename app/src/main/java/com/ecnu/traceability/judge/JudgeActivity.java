package com.ecnu.traceability.judge;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ecnu.traceability.InfoToOneNet;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.data_analyze.LocationAnalysisService;
import com.ecnu.traceability.ePayment.EPayment;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntityDao;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntityDao;
import com.ecnu.traceability.transportation.Transportation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JudgeActivity extends AppCompatActivity {
    private DBHelper dbHelper = DBHelper.getInstance();
    private GPSJudgement gpsJudgement = null;
    private MACAddressJudge macAddressJudge = null;
    private InfoToOneNet oneNetDataSender = null;
    private Judge judgeUtils = null;

    // 风险等级
    private int RISK_LEVEL = 0; //0:无风险 1:低风险 2:中风险 3:高风险
    private LinearLayout layout_high, layout_mid, layout_low, layout_zero;
    private TextView tvCurTime;
    private TextView tvBoard;

    // 展示数据
    private List<String> meetMacList;
    private ListView listViewCloseDevice;
    private List<String> meetTimeList;
    private ListView listViewCloseTime;
    private TextView tvMeetCount;

    // 上传数据
    private ImageButton btnUploadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge);
        getSupportActionBar().hide();
        dbHelper.init(this);
        gpsJudgement = new GPSJudgement(dbHelper);
        macAddressJudge = new MACAddressJudge(dbHelper);

        //初始化风险判断模块快
        judgeUtils = new Judge(getApplicationContext(), dbHelper);

        // 初始化界面
        initView();

        // 上传数据相关
        oneNetDataSender = new InfoToOneNet(dbHelper);

        //判断服务是否运行
        boolean serviceFlag = GeneralUtils.isServiceRunning(getApplicationContext(), "com.ecnu.traceability.data_analyze.LocationAnalysisService");
        if (!serviceFlag) {
            Intent intent = new Intent(this, LocationAnalysisService.class);
            bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);
        }


        // 计算当前设备的风险等级并更新UI
        checkCurDeviceRisk(0);
        updateRiskInfo();

        // 测试用，点击卡片切换风险等级
//        findViewById(R.id.cardview_risklevel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RISK_LEVEL = (RISK_LEVEL + 1) % 4;
//                updateRiskLevelLayout(RISK_LEVEL);
//            }
//        });
        btnUploadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "信息上传中...", Toast.LENGTH_LONG).show();
                upload();
            }
        });

    }

    public void updateRiskInfo() {
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(10000);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            // 检查是否有变化
                            double risk = judgeUtils.getRisk();

                            Log.i("judgeActivity risk", String.valueOf(risk));
                            checkCurDeviceRisk(risk);
                        }
                    });

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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

    /**
     * 判断当前设备的风险等级并更新UI
     */
    private void checkCurDeviceRisk(double risk) {
        // fake data
        meetMacList = new ArrayList<>();
        meetTimeList = new ArrayList<>();

        //        meetMacList = macAddressJudge.getMeetMacList();
        //        meetTimeList = gpsJudgement.judge();


        // 风险等级判断
        // 判断方法待补充 TODO
        // ... ...
        // 给RISK_LEVEL赋值即可更新风险等级【0:无风险 1:低风险 2:中风险 3:高风险】

        if (risk == 0) {
            RISK_LEVEL = 0;
        } else if (risk < 2) {
            RISK_LEVEL = 1;
        } else if (risk >= 3) {
            RISK_LEVEL = 3;
        }

        //        RISK_LEVEL = 3;

        // 更新sharedpreference中的风险等级
        SharedPreferences.Editor editor = getSharedPreferences("risk_data", MODE_PRIVATE).edit();
        editor.putInt("RISK_LEVEL", RISK_LEVEL);
        editor.apply();

        // 只要不是无风险，就显示【上传数据按钮】
        if (RISK_LEVEL != 0) {
            btnUploadData.setVisibility(View.VISIBLE);
        } else {
            btnUploadData.setVisibility(View.INVISIBLE);
        }

        // 更新风险图标
        updateRiskLevelLayout(RISK_LEVEL);

        // 更新具体信息
        DeviceAdapter deviceAdapter = new DeviceAdapter(getApplicationContext(), meetMacList);
        listViewCloseDevice.setAdapter(deviceAdapter);

        TimeAdapter timeAdapter = new TimeAdapter(getApplicationContext(), meetTimeList);
        listViewCloseTime.setAdapter(timeAdapter);

        tvMeetCount.setText(meetTimeList.size() + "");

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

    private void initView() {
        layout_high = findViewById(R.id.layout_high_risk);
        layout_mid = findViewById(R.id.layout_mid_risk);
        layout_low = findViewById(R.id.layout_low_risk);
        layout_zero = findViewById(R.id.layout_zero_risk);
        tvCurTime = findViewById(R.id.tv_risklevel_curtime);
        listViewCloseDevice = findViewById(R.id.lv_close_device);
        listViewCloseTime = findViewById(R.id.lv_close_time);
        tvMeetCount = findViewById(R.id.tv_meet_time_count);
        btnUploadData = findViewById(R.id.btn_upload_data);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        tvCurTime.setText(simpleDateFormat.format(date));
    }

    // 改变风险等级UI
    public void updateRiskLevelLayout(int risk_level) {
        switch (risk_level) {
            case 0:// 无风险
                layout_high.setVisibility(View.INVISIBLE);
                layout_mid.setVisibility(View.INVISIBLE);
                layout_low.setVisibility(View.INVISIBLE);
                layout_zero.setVisibility(View.VISIBLE);
                break;
            case 1:// 低风险
                layout_high.setVisibility(View.INVISIBLE);
                layout_mid.setVisibility(View.INVISIBLE);
                layout_low.setVisibility(View.VISIBLE);
                layout_zero.setVisibility(View.INVISIBLE);
                break;
            case 2:// 中风险
                layout_high.setVisibility(View.INVISIBLE);
                layout_mid.setVisibility(View.VISIBLE);
                layout_low.setVisibility(View.INVISIBLE);
                layout_zero.setVisibility(View.INVISIBLE);
                break;
            case 3:// 高风险
                layout_high.setVisibility(View.VISIBLE);
                layout_mid.setVisibility(View.INVISIBLE);
                layout_low.setVisibility(View.INVISIBLE);
                layout_zero.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
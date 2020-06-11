package com.ecnu.traceability.judge;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecnu.traceability.InfoToOneNet;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisUtil;
import com.ecnu.traceability.data_analyze.LocationAnalysisService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JudgeActivity extends AppCompatActivity {
    private DBHelper dbHelper = DBHelper.getInstance();
    private GPSJudgement gpsJudgement = null;
    private MACAddressJudge macAddressJudge = null;
    private InfoToOneNet oneNetDataSender = null;

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

        // 初始化界面
        initView();

        // 上传数据相关
        oneNetDataSender = new InfoToOneNet(dbHelper);
        Intent intent = new Intent(this, LocationAnalysisService.class);
        bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);

        // 计算当前设备的风险等级并更新UI
        checkCurDeviceRisk();

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
                uploadDataToOnenet();
            }
        });

    }

    /**
     * 判断当前设备的风险等级并更新UI
     */
    private void checkCurDeviceRisk() {
        // fake data
        meetMacList = new ArrayList<>();
        meetTimeList = new ArrayList<>();

        meetMacList = macAddressJudge.getMeetMacList();
        meetTimeList = gpsJudgement.judge();

        // 风险等级判断
        // 判断方法待补充 TODO
        // ... ...
        // 给RISK_LEVEL赋值即可更新风险等级【0:无风险 1:低风险 2:中风险 3:高风险】
        RISK_LEVEL = 3;

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

        tvMeetCount.setText(meetTimeList.size()+"");

    }
    private static final String MSG_CONTENT = "getAddress";
    private static final int MSG_ID_CLIENT = 1;
    private static final int MSG_ID_SERVER = 2;
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
//                Log.e("IPC", "Message from server: " + locationMap.size());
//                onResciverData(locationMap);
                oneNetDataSender.pushLocationMapData(locationMap);
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

    private void uploadDataToOnenet() {
        // 发送地图数据到onenet
        oneNetDataSender.pushMapDateToOneNet();

        // 主动上报数据不放在这里了
        // 主动上报数据的按钮放在了首页的右上角

        // 上传地点统计到onenet
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

        // 上传接触人数统计到onenet
        BluetoothAnalysisUtil util = new BluetoothAnalysisUtil(dbHelper);
        Bundle bundle1 = util.processData();
        Integer[] ans = (Integer[]) bundle1.get("countMap");
        ArrayList dateList = (ArrayList) bundle1.get("dateList");
        for (int i = 0; i < dateList.size(); i++) {
            Log.e("数据：", String.valueOf(ans[i]));
            Log.e("日期数据：", (String) dateList.get(i));
        }

        // 测试向oneNet提交信息
        oneNetDataSender.pushReportAndpersonCountData();
    }

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
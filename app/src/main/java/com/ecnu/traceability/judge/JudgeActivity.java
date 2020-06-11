package com.ecnu.traceability.judge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JudgeActivity extends AppCompatActivity {
    private DBHelper dbHelper = DBHelper.getInstance();
    private GPSJudgement gpsJudgement = null;
    private MACAddressJudge macAddressJudge = null;

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

        // 计算当前设备的风险等级并更新UI
        checkCurDeviceRisk();

        // 测试用，点击卡片切换风险等级
        findViewById(R.id.cardview_risklevel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RISK_LEVEL = (RISK_LEVEL + 1) % 4;
                updateRiskLevelLayout(RISK_LEVEL);
            }
        });

    }

    /**
     * 判断当前设备的风险等级并更新UI
     */
    private void checkCurDeviceRisk() {
        // fake data
        meetMacList = new ArrayList<>();
//        meetMacList.add("54:33:CB:8A:22:E1");
//        meetMacList.add("54:33:CB:8A:22:E1");
//        meetMacList.add("54:33:CB:8A:22:E1");
//        meetMacList.add("54:33:CB:8A:22:E1");
//        meetMacList.add("54:33:CB:8A:22:E1");
//        meetMacList.add("54:33:CB:8A:22:E1");
//        meetMacList.add("54:33:CB:8A:22:E1");


//        meetTimeList = new ArrayList<>();
//        meetTimeList = gpsJudgement.judge();
//        meetTimeList.add("2020-06-08 19:08:12");

        // 风险等级判断
        // 给RISK_LEVEL赋值即可更新风险等级【0:无风险 1:低风险 2:中风险 3:高风险】
        RISK_LEVEL = 3;

        // 更新风险图标
        updateRiskLevelLayout(RISK_LEVEL);

        meetMacList = new ArrayList<>();
        meetTimeList = new ArrayList<>();
        // 更新具体信息
        DeviceAdapter deviceAdapter = new DeviceAdapter(getApplicationContext(), meetMacList);
        listViewCloseDevice.setAdapter(deviceAdapter);

        TimeAdapter timeAdapter = new TimeAdapter(getApplicationContext(), meetTimeList);
        listViewCloseTime.setAdapter(timeAdapter);

        tvMeetCount.setText(meetTimeList.size()+"");

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
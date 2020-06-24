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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ecnu.traceability.BaseActivity;
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
import com.ecnu.traceability.location.service.FencesService;
import com.ecnu.traceability.machine_learning.Learning;
import com.ecnu.traceability.machine_learning.LearningData;
import com.ecnu.traceability.machine_learning.LearningDataDao;
import com.ecnu.traceability.machine_learning.TrainModel;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntityDao;
import com.ecnu.traceability.transportation.Transportation;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transformation.TransformationChildCard;

import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JudgeActivity extends BaseActivity {

    public static final int MAX_RISK_LEVEL = 2;
    private static final int[] sampleShape = {1, 6};//数据集格式是一行三列不包括label

    private DBHelper dbHelper = DBHelper.getInstance();
    private GPSJudgement gpsJudgement = null;
    private MACAddressJudge macAddressJudge = null;
    private InfoToOneNet oneNetDataSender = null;
    private Judge judgeUtils = null;

    // 风险等级
    private int RISK_LEVEL = 0; //0:无风险 1:低风险 2:中风险 3:高风险
    private Learning learning;


    private LinearLayout layout_high, layout_mid, layout_low, layout_zero;
    private TextView tvCurTime;
    private TextView tvBoard;

    // 展示数据
    private List<String> meetMacList;
    private List<LocationEntity> meetTimeList;
    private List<TransportationEntity> transList;
    private ListView listViewCloseDevice;
    private ListView listViewCloseTime;
    private ListView listViewCloseTrans;
    private TextView tvMeetCount;

    // 上传数据
    private ImageButton btnUploadData;

    private double risk = 0.0;

    // TabLayout
    private TabLayout closeTablayout;
    private LinearLayout layoutDevice;
    private LinearLayout layoutTime;
    private LinearLayout layoutTrans;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("judgeActivity", "============================");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge);
        getSupportActionBar().hide();
        dbHelper.init(this);
        gpsJudgement = new GPSJudgement(dbHelper);
        macAddressJudge = new MACAddressJudge(dbHelper);

        learning = new Learning();

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

        //初始化风险项目列表
        meetMacList = new ArrayList<>();
        meetTimeList = new ArrayList<>();
        transList = new ArrayList<>();

        // 假数据 放着备用
//        meetMacList.add("EC:51:BC:AE:15:7E");
//        meetMacList.add("D8:CE:3A:86:DA:27");
//        meetMacList.add("D9:CE:3A:86:DA:27");
//        meetMacList.add("EC:51:BC:AE:15:7E");
//        meetMacList.add("54:33:CB:8A:22:E1"); // 我滴手机
//        meetMacList.add("B8:C9:B5:36:03:1C"); // 本地的
//        meetMacList.add("E0:1F:88:D9:C5:9E"); // 邻居手机
//        meetMacList.add("14:23:3B:8A:22:E2"); // 瞎编的
//        meetMacList.add("25:13:1B:1A:12:1E"); // 瞎编的
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1592208057749L)));
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1592208057729L)));
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1592051321615L)));
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1591923873393L)));
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1591838738234L)));
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1591623434324L)));
//        meetTimeList.add(new LocationEntity(29.11, 115.22, new Date(1591598356273L)));
//
//        transList.add(new TransportationEntity("火车", "G123", 056, new Date()));
//        transList.add(new TransportationEntity("火车", "G247", 12, new Date()));

        // 计算当前设备的风险等级并更新UI
        checkCurDeviceRisk(0);
        updateRiskInfo();

        // 测试用，点击卡片切换风险等级
        findViewById(R.id.cardview_risklevel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRisk();
//                RISK_LEVEL = (RISK_LEVEL + 1) % 4;
//                updateRiskLevelLayout(RISK_LEVEL);
            }
        });
        btnUploadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "信息上传中...", Toast.LENGTH_LONG).show();
                upload();
            }
        });


        // TabLayout回调
        closeTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0://设备
                        layoutDevice.setVisibility(View.VISIBLE);
                        layoutTime.setVisibility(View.GONE);
                        layoutTrans.setVisibility(View.GONE);
                        break;
                    case 1://时间
                        layoutDevice.setVisibility(View.GONE);
                        layoutTime.setVisibility(View.VISIBLE);
                        layoutTrans.setVisibility(View.GONE);
                        break;
                    case 2://交通
                        layoutDevice.setVisibility(View.GONE);
                        layoutTime.setVisibility(View.GONE);
                        layoutTrans.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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
                            double newRisk = judgeUtils.getRisk();
                            if (newRisk > risk) {//选择最大的那个风险
                                risk = newRisk;
                                Bundle bundle = judgeUtils.getDateForFederatedLearnging();
                                infer(bundle);
                            }
                            //更新列表
                            meetMacList = new ArrayList<>();
                            meetTimeList = new ArrayList<>();
                            transList = new ArrayList<>();
                            meetMacList = judgeUtils.getPatientMacList();
                            meetTimeList = judgeUtils.getSameLocationList();
                            transList = judgeUtils.getSameTransportation();
                            // 更新当前的风险等级
                            checkCurDeviceRisk(risk);
                            Log.i("judgeActivity risk", String.valueOf(risk));
                        }
                    });

                    if (risk == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateRisk();
                                checkCurDeviceRisk(risk);

                            }
                        });
                        Log.i("judgeActivity", "使用本地数据更新risk");
                        break;
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 根据数据库中的历史数据更新risk
     *
     * @return
     */
    public void updateRisk() {
        Bundle bundle = judgeUtils.judgeFormHistory();
        List<MacRisk> macRisks = (List<MacRisk>) bundle.get("macRisks");
        List<TransRisk> transRisks = (List<TransRisk>) bundle.get("transRisks");
        List<GPSRisk> gpsRisks = (List<GPSRisk>) bundle.get("gpsRisks");

        //更新列表
        for (MacRisk mr : macRisks) {
            meetMacList.add(mr.getMacAddress());
        }
        for (GPSRisk gpsRisk : gpsRisks) {
            meetTimeList.add(new LocationEntity(gpsRisk.getLatitude(), gpsRisk.getLongitude(), gpsRisk.getDate()));
        }
        for (TransRisk tr : transRisks) {
            transList.add(new TransportationEntity(tr.type, tr.NO, tr.seat, tr.date));
        }
        List<LearningData> dataList = dbHelper.getSession().getLearningDataDao().queryBuilder().orderDesc(LearningDataDao.Properties.Date).limit(1).list();
        if (null != dataList && dataList.size() > 0) {
            risk = dataList.get(0).getLabel();
        } else {
            risk = 0;
        }
        checkCurDeviceRisk(risk);

//        double tempRisk = macRisks.size() * 3 + transRisks.size() * 2 + gpsRisks.size();
//        if (tempRisk > risk) {//如果已保存的风险高于当前从服务器接收的则使用历史风险
//            risk = tempRisk;
//        }
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

            learning.updateLearningData(true, dbHelper);//更新联邦学习数据
            learning.propocessData(learning.getLearningDataFromDB(dbHelper));
            learning.startLearning();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void infer(Bundle bundle) {

        double avgStrength = bundle.getDouble("avgStrength", 0);
        double bluetoothTime = bundle.getDouble("bluetoothTime", 0);
        double transCount = bundle.getDouble("transCount", 0);
        double avgSeatDiff = bundle.getDouble("avgSeatDiff", 0);
        avgSeatDiff = (transCount == 0 ? 10 : (avgSeatDiff * 0.01));
        double avgDistance = bundle.getDouble("avgDistance", 0);
        avgDistance = (avgDistance == 0 ? 51 : avgDistance);
        double gpsTime = bundle.getDouble("gpsTime", 0);

        double[] dataArray = {avgStrength, bluetoothTime, transCount, avgSeatDiff, avgDistance, gpsTime};

        //等待生成数据和加载模型
        double finalAvgSeatDiff = avgSeatDiff;
        double finalAvgDistance = avgDistance;
        new Thread(() -> {
            try {
                if (TrainModel.model == null) {
                    Log.i("Learning", "model is not loaded yet");
                    Log.e("loading model", "正在尝试加载模型");
                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(
                            TrainModel.locateToLoadModel, false);
                    Log.e("loading model", "已经加载模型");
                }

                INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(dataArray), sampleShape);
                INDArray predicted = TrainModel.model.output(sample_to_infer, false);
                INDArray index = predicted.argMax();
                int[] pl = index.toIntVector();
                int result = pl[0];
                Log.e("federated learning", "推断结果是" + result);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateStr = sdf.format(new Date());
                Date date = sdf.parse(dateStr);

                LearningData learningData = new LearningData(result, avgStrength, bluetoothTime, transCount, finalAvgSeatDiff, finalAvgDistance, gpsTime, date);
                dbHelper.getSession().getLearningDataDao().insert(learningData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 判断当前设备的风险等级并更新UI
     */
    private void checkCurDeviceRisk(double risk) {
        // fake data
        // meetMacList = new ArrayList<>();
        // meetTimeList = new ArrayList<>();
        // meetMacList = judgeUtils.getPatientMacList();
        // meetTimeList = judgeUtils.getSameLocationList();
        //        meetMacList = macAddressJudge.getMeetMacList();
        //        meetTimeList = gpsJudgement.judge();


        // 风险等级判断
        // 判断方法待补充 TODO
        // ... ...
        // 给RISK_LEVEL赋值即可更新风险等级【0:无风险 1:低风险 2:中风险 3:高风险】

        //暂时先这样写，有时间尝试换成机器学习算法。
        //        if (risk <= 0) {
        //            RISK_LEVEL = 0;
        //        } else if (risk < 2) {
        //            RISK_LEVEL = 1;
        //        } else if (risk >= 3 && risk < 10) {
        //            RISK_LEVEL = 2;
        //        } else {
        //            RISK_LEVEL = 3;
        //        }

        RISK_LEVEL = (int) risk;
        fence(RISK_LEVEL);
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

        TransAdapter transAdapter = new TransAdapter(getApplicationContext(), transList);
        listViewCloseTrans.setAdapter(transAdapter);
//        tvMeetCount.setText(meetTimeList.size() + "");

    }
    //判断是否需要开启围栏
    public void fence(int RISK_LEVEL){
        Intent fencesIntent = new Intent(this, FencesService.class);
        switch (RISK_LEVEL) {
            case 0:
                break;
            case 1:
                //围栏服务
                startService(fencesIntent);//开启围栏，出去围栏实时自动定位
                break;
            case 2:
                //围栏服务
                startService(fencesIntent);//开启围栏，出去围栏实时自动定位
                break;
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

    private void initView() {
        layout_high = findViewById(R.id.layout_high_risk);
        layout_mid = findViewById(R.id.layout_mid_risk);
        layout_low = findViewById(R.id.layout_low_risk);
        layout_zero = findViewById(R.id.layout_zero_risk);
        tvCurTime = findViewById(R.id.tv_risklevel_curtime);
        listViewCloseDevice = findViewById(R.id.lv_close_device);
        listViewCloseTime = findViewById(R.id.lv_close_time);
        listViewCloseTrans = findViewById(R.id.lv_close_trans);
//        tvMeetCount = findViewById(R.id.tv_meet_time_count);
        btnUploadData = findViewById(R.id.btn_upload_data);
        // tablayout
        closeTablayout = findViewById(R.id.close_tablayout);
        layoutDevice = findViewById(R.id.layout_close_device);
        layoutTime = findViewById(R.id.layout_close_time);
        layoutTrans = findViewById(R.id.layout_close_trans);

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
                layout_mid.setVisibility(View.VISIBLE);
                layout_low.setVisibility(View.INVISIBLE);
                layout_zero.setVisibility(View.INVISIBLE);
//                layout_high.setVisibility(View.INVISIBLE);
//                layout_mid.setVisibility(View.INVISIBLE);
//                layout_low.setVisibility(View.VISIBLE);
//                layout_zero.setVisibility(View.INVISIBLE);
                break;
            case 2:// 中风险
                layout_high.setVisibility(View.VISIBLE);
                layout_mid.setVisibility(View.INVISIBLE);
                layout_low.setVisibility(View.INVISIBLE);
                layout_zero.setVisibility(View.INVISIBLE);
//                layout_high.setVisibility(View.INVISIBLE);
//                layout_mid.setVisibility(View.VISIBLE);
//                layout_low.setVisibility(View.INVISIBLE);
//                layout_zero.setVisibility(View.INVISIBLE);
                break;
//            case 3:// 高风险
//                layout_high.setVisibility(View.VISIBLE);
//                layout_mid.setVisibility(View.INVISIBLE);
//                layout_low.setVisibility(View.INVISIBLE);
//                layout_zero.setVisibility(View.INVISIBLE);
//                break;
        }
    }

}
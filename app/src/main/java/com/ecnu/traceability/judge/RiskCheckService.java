package com.ecnu.traceability.judge;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.NotificationUtil;
import com.ecnu.traceability.machine_learning.Learning;
import com.ecnu.traceability.machine_learning.LearningData;
import com.ecnu.traceability.machine_learning.TrainModel;

import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RiskCheckService extends Service {
    private boolean flag = false;
    private DBHelper dbHelper = DBHelper.getInstance();
    private Judge judgeUtils = null;

    private int TIME_INTERVAL = 1000 * 3600 * 12; // 这是12小时

    private static final int[] sampleShape = {1, 6};//数据集格式是一行三列不包括label

    private int waitCount = 0;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter(TEST_ACTION);
        registerReceiver(receiver, intentFilter);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(TEST_ACTION);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        dbHelper.init(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0低电量模式需要使用该方法触发定时任务
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4以上 需要使用该方法精确执行时间
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else {//4.4以下 使用老方法
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), TIME_INTERVAL, pendingIntent);
        }
    }

    public static final String TEST_ACTION = "RISK_CHECK_ACTION";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TEST_ACTION.equals(action)) {
                //执行特定的任务
                //初始化风险判断模块快
                waitCount = 0;
                //下载模型，检查风险等级
                if (flag) {//第一次启动时不下载
                    judgeUtils = new Judge(getApplicationContext(), dbHelper);//查询风险
                    updateRiskInfo();
                    Learning learning = new Learning();
                    learning.downloadModel();
                }
                flag = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void updateRiskInfo() {
        new Thread(() -> {
            try {
                while (waitCount < 3) {
                    Thread.sleep(10000);
                    // 检查是否有变化
                    double newRisk = judgeUtils.getRisk();
                    if (newRisk > 0) {//选择最大的那个风险
                        //risk = newRisk;
                        Bundle bundle = judgeUtils.getDateForFederatedLearnging();
                        infer(bundle);
                        break;
                    }
                    waitCount++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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

                if (result > 0) {
                    //显示通知
                    NotificationUtil.notification(this, "风险警告", "你已经接触病毒患者，请进行自我隔离",1);
                }
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

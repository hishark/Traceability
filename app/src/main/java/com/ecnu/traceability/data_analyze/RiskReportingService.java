package com.ecnu.traceability.data_analyze;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.judge.MACAddressJudge;
import com.ecnu.traceability.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RiskReportingService extends Service {
    private DBHelper dbHelper = DBHelper.getInstance();

    int TIME_INTERVAL = 1000 * 3600 * 12; // 这是12小时
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    // int TIME_INTERVAL = 1000; // 这是12小时
    // 当前设备的手机号
    //String curTel;

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

    public static final String TEST_ACTION = "_TEST_ACTION";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TEST_ACTION.equals(action)) {
                //执行特定的任务
                //查询当前的风险等级，如果风险等级为高，主动向疫情防控服务器发送自己的手机号
                SharedPreferences sharedPreferences = getSharedPreferences("risk_data", MODE_PRIVATE);
                int RISK_LEVEL = sharedPreferences.getInt("RISK_LEVEL", 0);
                Log.e("telephone", String.valueOf(RISK_LEVEL));
                // 风险等级为3 - 高风险
                if (RISK_LEVEL == 2) {
                    // 从SIM卡获取手机号
                    //getCurPhoneNum();
                    // 向服务器发送自己的手机号
                    String tel = getTel();
                    pushTelToServer(tel);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
                }
            }
        }
    };

    private void pushTelToServer(String tel) {
        HTTPUtils.addTelephone(tel);
    }

    private String getTel() {
        List<User> users = dbHelper.getSession().getUserDao().loadAll();
        if (null != users && users.size() > 0) {
            User user = users.get(0);
            return user.getTel();
        } else {
            return null;
        }
    }
    //    private void getCurPhoneNum() {
    //        Log.e("telephone test", "=======================" );
    //        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
    //        curTel = manager.getLine1Number() != null ? manager.getLine1Number() : "无法获取到手机号";
    ////        curTel = manager.getLine1Number() != null ? manager.getLine1Number() : "can't get phone number";
    //        // 查看log是否成功取到了手机号，
    //        Log.e("telephone", curTel);
    //        Log.e("telephone test", "===========--==========" );
    //
    //    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

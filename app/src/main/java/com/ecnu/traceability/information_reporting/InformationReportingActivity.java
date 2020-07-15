package com.ecnu.traceability.information_reporting;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecnu.traceability.InfoToOneNet;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.data_analyze.LocationAnalysisService;
import com.ecnu.traceability.ePayment.EPayment;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntityDao;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntityDao;
import com.ecnu.traceability.transportation.Transportation;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InformationReportingActivity extends AppCompatActivity implements Button.OnClickListener {
    private static final String TAG = "InformationReporting";
    private InfoToOneNet oneNetDataSender = null;

    private EditText text = null;
    private String date_ = null;
    private String time_ = null;
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_reporting);
        setTitle("主动上报信息");
        findViewById(R.id.info_reporting_btn).setOnClickListener(this);
        dbHelper.init(this);
        oneNetDataSender = new InfoToOneNet(dbHelper);//发送到OneNet信息的工具类
        Intent intent = new Intent(this, LocationAnalysisService.class);
        bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);

        text = findViewById(R.id.info_reporting_text);
        text.setHeight(300);
        Calendar now = Calendar.getInstance();
        android.app.DatePickerDialog dateDialog = new android.app.DatePickerDialog(
                this,
                (view1, year, month, dayOfMonth) -> {
                    date_ = year + "-" + month + "-" + dayOfMonth;
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        android.app.TimePickerDialog timeDialog = new android.app.TimePickerDialog(
                this,
                (view11, hour, minute) -> {
                    time_ = hour + ":" + minute + ":" + 0;
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );


        Button dateBtn = findViewById(R.id.info_reporting_date);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeDialog.show();
                dateDialog.show();
            }
        });


        // 删去【提交信息到服务器】按钮，代码暂时注释以防之后需要
//        Button testBtn = findViewById(R.id.info_reporting_to_server);
//        testBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Transportation transInfo=new Transportation(dbHelper);
//                EPayment paymentInfo=new EPayment(dbHelper);
//                transInfo.addTransportationInfo();
//                paymentInfo.addEPaymentInfo();
//
//                Message message = Message.obtain();
//                message.arg1 = MSG_ID_CLIENT;
//                Bundle bundle = new Bundle();
//                bundle.putString(MSG_CONTENT, "测试信息");
//                message.setData(bundle);
//                message.replyTo = mClientMessenger;     //指定回信人是客户端定义的
//
//                try {
//                    mServerMessenger.send(message);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
////                sendInfoToServer();
//            }
//        });

    }

    @Override
    public void onClick(View view) {
        //字段验证
        String info = text.getText().toString();
        if (info.equals("") || null == info) {
            text.setHint("信息不能为空");
            return;
        }
        if (null == date_) {
            GeneralUtils.showToastInService(getApplicationContext(), "请选择日期");
            return;
        }
        if (null == time_) {
            GeneralUtils.showToastInService(getApplicationContext(), "请选择时间");
            return;
        }

        String date=date_+" "+time_;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateProcessed;
        try {
            dateProcessed=sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            dateProcessed=null;
        }
        Log.e("data info ",sdf.format(dateProcessed));
        ReportInfoEntity entity = new ReportInfoEntity(info, dateProcessed);
        dbHelper.getSession().getReportInfoEntityDao().insert(entity);
        GeneralUtils.showToastInService(getApplicationContext(),"添加成功！");

        //继续初始化
        text.setText("");
        this.date_=null;
        this.time_=null;
    }


    //    ---------------------------------------------测试用-------------------------------------------------
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
                ////////////////////////////////////////////////////////
                Map<String, Integer> locationMap = (Map<String, Integer>) msg.getData().get(MSG_CONTENT);
                oneNetDataSender.sendPieChartData(locationMap);//向OneNet发送地点（地图）统计信息
                ////////////////////////////////////////////////////////
                List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
                oneNetDataSender.pushMapDateToOneNet(locationList);//向OneNet地点统计（饼图）发送信息
                ////////////////////////////////////////////////////////

                List<ReportInfoEntity> reportInfoList = dbHelper.getSession().getReportInfoEntityDao().queryBuilder()
                        .orderAsc(ReportInfoEntityDao.Properties.Date).list();
                oneNetDataSender.sendReportInfoToOneNet(reportInfoList);//向OneNet发送人数统计和主动上报的公告板信息（公告板和条形图）
                ////////////////////////////////////////////////////////
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

}
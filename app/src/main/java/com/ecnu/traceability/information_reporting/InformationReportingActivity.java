package com.ecnu.traceability.information_reporting;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntityDao;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InformationReportingActivity extends AppCompatActivity implements Button.OnClickListener {
    private static final String TAG = "InformationReporting";

    private EditText text = null;
    private String date_ = null;
    private String time_ = null;
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_reporting);
        setTitle("主动上报数据");
        findViewById(R.id.info_reporting_btn).setOnClickListener(this);
        dbHelper.init(this);

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

//        Button testBtn = findViewById(R.id.info_reporting_btn_one_net);
//        testBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendInfoToServer();
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

        String[] dataArray = date_.split("-");
        String[] timeArray = time_.split(":");
        Date date = new Date(Integer.parseInt(dataArray[0]), Integer.parseInt(dataArray[1]), Integer.parseInt(dataArray[2]),
                Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]), Integer.parseInt(timeArray[2]));

        ReportInfoEntity entity = new ReportInfoEntity(info, date);
        dbHelper.getSession().getReportInfoEntityDao().insert(entity);
        GeneralUtils.showToastInService(getApplicationContext(),"添加成功！");

        //继续初始化
        text.setText("");
        this.date_=null;
        this.time_=null;
    }

//    public void sendInfoToServer() {
//        List<ReportInfoEntity> reportInfoList = dbHelper.getSession().getReportInfoEntityDao().queryBuilder()
//                .orderAsc(ReportInfoEntityDao.Properties.Date).list();
//        String deviceId = "601016239";
//        String datastream = "data_flow_2";
//        JSONArray datapoints = new JSONArray();
//        try {
//            for (ReportInfoEntity reportFromDB : reportInfoList) {
//
//                SimpleDateFormat sfd=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                Log.e(TAG,sfd.format(reportFromDB.getDate()));
//                JSONObject reportInfo = new JSONObject();
//                reportInfo.put("MacAddress", MacAddress.getBtAddressByReflection());
//                reportInfo.put("Description",reportFromDB.getText());
//                reportInfo.put("Date",sfd.format(reportFromDB.getDate()));
//
//                JSONObject datapoint = new JSONObject();
//                datapoint.putOpt("value", reportInfo);
//                datapoints.put(datapoint);
//            }
//
//            JSONObject dsObject = new JSONObject();
//            dsObject.putOpt("id", datastream);
//            dsObject.putOpt("datapoints", datapoints);
//
//            JSONArray datastreams = new JSONArray();
//            datastreams.put(dsObject);
//
//            JSONObject request = new JSONObject();
//            request.putOpt("datastreams", datastreams);
//            OneNetDeviceUtils.sendData(deviceId, request);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        String url="";//网址加mac地址
//        HTTPUtils.sendByOKHttp("", data, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//            }
//        });
//    }

}
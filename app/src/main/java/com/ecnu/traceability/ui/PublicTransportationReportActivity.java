package com.ecnu.traceability.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ecnu.traceability.BaseActivity;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PublicTransportationReportActivity extends BaseActivity {


    private String date_ = null;
    private String time_ = null;

    private EditText etTrans;
    private EditText etTransId;
    private EditText etTransSeatNum;

    private Button btTimeSelect;
    private Button btSubmit;

    private DBHelper dbHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transportation_info_report);
        setTitle("");

        initView();

        dbHelper = DBHelper.getInstance();
        dbHelper.init(this);

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

        btTimeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog.show();
                dateDialog.show();
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String transType = etTrans.getText().toString().trim();
                String transId = etTransId.getText().toString().trim();
                String transSeatNum = etTransSeatNum.getText().toString().trim();
                if (transType.equals("") || transId.equals("") || transSeatNum.equals("")) {
                    GeneralUtils.showToastInService(getApplicationContext(), "以上信息不能为空");
                    return;
                }

                if (date_ == null) {
                    GeneralUtils.showToastInService(getApplicationContext(), "请选择日期");
                    return;
                }
                if (time_ == null) {
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
                TransportationEntity entity = new TransportationEntity(transType, transId, Integer.parseInt(transSeatNum), dateProcessed);
                dbHelper.getSession().getTransportationEntityDao().insert(entity);
                GeneralUtils.showToastInService(getApplicationContext(),"提交成功 ");

                //继续初始化
                etTrans.setText("");
                etTransId.setText("");
                etTransSeatNum.setText("");
                date_=null;
                time_=null;

            }
        });


    }

    private void initView() {
        etTrans = findViewById(R.id.et_trans_type);
        etTransId = findViewById(R.id.et_trans_id);
        etTransSeatNum = findViewById(R.id.et_trans_seat_num);
        btTimeSelect = findViewById(R.id.bt_timeselect);
        btSubmit = findViewById(R.id.bt_submit);
    }
}

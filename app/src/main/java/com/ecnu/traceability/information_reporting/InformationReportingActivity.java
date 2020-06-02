package com.ecnu.traceability.information_reporting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InformationReportingActivity extends AppCompatActivity implements Button.OnClickListener {

    private EditText text = null;
    private EditText date = null;
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_reporting);
        findViewById(R.id.info_reporting_btn).setOnClickListener(this);
        dbHelper.init(this);

        text = findViewById(R.id.info_reporting_text);
        date = findViewById(R.id.info_reporting_date);
    }

    @Override
    public void onClick(View view) {
        String info = text.getText().toString();
        Date date = new Date(text.getText().toString());
        dbHelper.getSession().getReportInfoEntityDao().insert(new ReportInfoEntity(info, date));

    }

    public void sendInfoToServer(RequestBody data) {
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
    }
}
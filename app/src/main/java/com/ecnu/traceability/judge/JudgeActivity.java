package com.ecnu.traceability.judge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;

public class JudgeActivity extends AppCompatActivity {
    private DBHelper dbHelper = DBHelper.getInstance();
    private GPSJudgement gpsJudgement=null;
    private MACAddressJudge macAddressJudge=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge);
        dbHelper.init(this);
        gpsJudgement=new GPSJudgement(dbHelper);
        macAddressJudge=new MACAddressJudge(dbHelper);
    }



}
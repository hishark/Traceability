package com.ecnu.traceability.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.ecnu.traceability.BaseActivity;
import com.ecnu.traceability.R;
import com.ecnu.traceability.information_reporting.InformationReportingActivity;

public class UserReportActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_report);
        setTitle("信息主动上报");


        findViewById(R.id.cardview_public_transportation_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(PublicTransportationReportActivity.class);
            }
        });

        findViewById(R.id.cardview_e_payment_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(EPaymentReportActivity.class);
            }
        });

        findViewById(R.id.cardview_other_information_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(InformationReportingActivity.class);
            }
        });


    }
}
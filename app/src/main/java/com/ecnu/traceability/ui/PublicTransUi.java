package com.ecnu.traceability.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.judge.TransAdapter;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.util.List;

public class PublicTransUi extends AppCompatActivity {
    private ListView listViewCloseTrans;
    private DBHelper dbHelper=DBHelper.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("乘坐公共交通工具信息");
        setContentView(R.layout.activity_public_trans_ui);

        listViewCloseTrans = findViewById(R.id.lv_trans_info);
        dbHelper.init(PublicTransUi.this);

        showListView();
    }


    private void showListView(){

        List<TransportationEntity> list=dbHelper.getSession().getTransportationEntityDao().queryBuilder().list();

        TransAdapter transAdapter = new TransAdapter(getApplicationContext(), list);
        listViewCloseTrans.setAdapter(transAdapter);
    }
}
package com.ecnu.traceability.judge;

import android.content.Context;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Judge {
    private double risk = 0.0;
    private GPSJudgement gpsJudgement;
    private MACAddressJudge macAddressJudgement;
    private TransportationJudge transportationJudgement;
    private Context context;
    private DBHelper dbHelper;
    //所有患者的MAC地址
    private List<String> patientMacList;
    //本地数据库中的数据
    List<LocationEntity> localLocationData;
    List<TransportationEntity> localTransportationData;


    /**
     * 风险判别模块（整合了所有的风险）定时调用该方法的getRisk将会得到风险更新
     * @param context
     * @param dbHelper
     */
    public Judge(Context context, DBHelper dbHelper) {
        this.risk = 0.0;
        this.context = context;
        this.dbHelper = dbHelper;

        gpsJudgement = new GPSJudgement(dbHelper);
        macAddressJudgement = new MACAddressJudge(dbHelper);
        transportationJudgement = new TransportationJudge(dbHelper);
        localLocationData = gpsJudgement.getDataFromDatabase();
        localTransportationData = transportationJudgement.getDataFromDatabase();
        HTTPUtils.getPatientMacAddress(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("getPatientMacAddress", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                patientMacList = new ArrayList<String>();
                String responseBody = response.body().string();
                Log.i("getPatientMacAddress", "============"+responseBody+"============" );
                try {
                    JSONArray jsonArr = new JSONArray(responseBody);
                    Log.i("getPatientMacAddress", String.valueOf(jsonArr.length()));
                    for (int i = 0; i < jsonArr.length(); i++) {
                        String jsonObject = jsonArr.getString(i);
                        Log.i("patientMacList ", jsonObject);
                        patientMacList.add(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //////////////////////调用风险判别//////////////////////
                for (String patientMac : patientMacList) {
                    Log.i("judge ", "-----------------------------start judge-------------------------");

                    gpsJudgement.queryPatientLocationInfo(patientMac, locationCallback);
                    transportationJudgement.queryPatientTransportationinfo(patientMac, transportationCallback);
                    risk += macAddressJudgement.judge(macAddressJudgement.getDataFromDatabase(patientMac));
                    Log.i("mac risk", String.valueOf(risk));
                }


            }
        });

    }

    //////////////////////调用风险判别//////////////////////
    Callback locationCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.i("locationCallback", e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            List<LocationEntity> serverDataList = gpsJudgement.parseDate(response);
            risk += gpsJudgement.judge(serverDataList, localLocationData);
            Log.i("gps risk", String.valueOf(risk));
        }
    };
    //////////////////////调用风险判别//////////////////////
    Callback transportationCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.i("transportationCallback", e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            List<TransportationEntity> serverDataList = transportationJudgement.parseDateFormServer(response);
            risk += transportationJudgement.judge(serverDataList, localTransportationData);
            Log.i("transportation risk", String.valueOf(risk));

        }
    };


    public List<String> getPatientMacList() {
        if (null != patientMacList) {
            return patientMacList;
        } else {
            return new ArrayList<String>();
        }
    }

    public double getRisk() {
        return risk;
    }
}

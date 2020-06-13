package com.ecnu.traceability.judge;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
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
    private List<LocationEntity> localLocationData;
    private List<TransportationEntity> localTransportationData;
    //GPS位置相同的时间地点列表
    private List<LocationEntity> timeLocationList;
    //乘坐相同的交通工具列表
    private List<TransportationEntity> sameTransportationList;

    private int requestCount = 0;

    /**
     * 风险判别模块（整合了所有的风险）定时调用该方法的getRisk将会得到风险更新
     *
     * @param context
     * @param dbHelper
     */
    public Judge(Context context, DBHelper dbHelper) {
        requestCount = 0;
        this.risk = 0.0;
        this.context = context;
        this.dbHelper = dbHelper;

        gpsJudgement = new GPSJudgement(dbHelper);
        macAddressJudgement = new MACAddressJudge(dbHelper);
        transportationJudgement = new TransportationJudge(dbHelper);
        localLocationData = gpsJudgement.getDataFromDatabase();
        localTransportationData = transportationJudgement.getDataFromDatabase();
        //获取已经发现的病人的Mac地址列表
        HTTPUtils.getPatientMacAddress(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("getPatientMacAddress", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                timeLocationList = new ArrayList<>();
                sameTransportationList = new ArrayList<>();

                patientMacList = new ArrayList<String>();

                String responseBody = response.body().string();
                Log.i("getPatientMacAddress", "============" + responseBody + "============");
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
                    requestCount++;
                    transportationJudgement.queryPatientTransportationinfo(patientMac, transportationCallback);
                    requestCount++;
                    risk += macAddressJudgement.judge(macAddressJudgement.getDataFromDatabase(patientMac));
                    Log.i("mac risk", String.valueOf(risk));
                }

                judegeIsFinshed();//如果已经完成所有推送则向服务器发送已推送通知


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
            Bundle bundle = gpsJudgement.judge(serverDataList, localLocationData);
            List<LocationEntity> tempList = (List<LocationEntity>) bundle.get("gpsJudge");
            risk += tempList.size();
            timeLocationList.addAll(tempList);//增量式添加所有与所有接触者接触信息
            requestCount--;

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
//            sameTransportationList = transportationJudgement.judge(serverDataList, localTransportationData);
            List<TransportationEntity> tempList = transportationJudgement.judge(serverDataList, localTransportationData);
            risk += tempList.size() * 2;
            sameTransportationList.addAll(tempList);//增量式添加所有与所有接触者接触信息
            Log.i("transportation risk", String.valueOf(risk));
            requestCount--;
        }
    };

    public void judegeIsFinshed() {
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(2000);
                    if (requestCount == 0) {
                        for (String mac : patientMacList) {
                            //已经完成推送，向推送信息表添加信息
                            HTTPUtils.addPushedInfo(mac);
                        }
                        break;//停止判断
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

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

    public List<TransportationEntity> getSameTransportation() {
        if (null != sameTransportationList) {
            return sameTransportationList;
        } else {
            return new ArrayList<TransportationEntity>();
        }
    }

    public List<LocationEntity> getSameLocationList() {
        if (null != timeLocationList) {
            return timeLocationList;
        } else {
            return new ArrayList<LocationEntity>();
        }
    }


    public Bundle judgeFormHistory(){

        List<MacRisk> macRisks=dbHelper.getSession().getMacRiskDao().loadAll();
        List<TransRisk> transRisks=dbHelper.getSession().getTransRiskDao().loadAll();
        List<GPSRisk> gpsRisks=dbHelper.getSession().getGPSRiskDao().loadAll();
        Bundle bundle=new Bundle();
        bundle.putSerializable("macRisks", (Serializable) macRisks);
        bundle.putSerializable("transRisks", (Serializable) transRisks);
        bundle.putSerializable("gpsRisks", (Serializable) gpsRisks);
        return bundle;
    }

}

package com.ecnu.traceability.judge;

import android.os.Bundle;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.DateUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TransportationJudge {
    private DBHelper dbHelper = null;
    private List<TransportationEntity> dataFromServer = null;

    private double transCount = 0;
    private double avgSeatDiff = 0.0;

    public TransportationJudge(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    public void queryPatientTransportationinfo(String patientAddress, Callback locationCallback) {
        HTTPUtils.queryPatientTransportationinfo(patientAddress, locationCallback);
    }

    public List<TransportationEntity> getDataFromDatabase() {
        List<TransportationEntity> list = dbHelper.getSession().getTransportationEntityDao().loadAll();
        return list;
    }

    public void addRiskToDB(TransportationEntity entity) {
        dbHelper.getSession().getTransRiskDao().insert(new TransRisk(entity.getType(), entity.getNO(), entity.getSeat(), entity.getDate()));
    }

    public Bundle judge(List<TransportationEntity> patientDataList, List<TransportationEntity> locDataList) {

//        transCount = 0;
//        avgSeatDiff = 0.0;

        int count = 0;

        List<TransportationEntity> sameList = new ArrayList<>();
        for (TransportationEntity serData : patientDataList) {
            for (TransportationEntity locData : locDataList) {
                if (serData.getNO().equals(locData.getNO())) {
                    if (DateUtils.dataDiff(serData.getDate(), locData.getDate()) < 200) {
                        sameList.add(serData);
                        addRiskToDB(serData);//将风险信息保存到数据库
                        count++;

                        if(serData.getSeat()!=-1&&locData.getSeat()!=-1){
                            avgSeatDiff+=Math.abs(serData.getSeat()-locData.getSeat());
                        }else{
                            avgSeatDiff+=5;
                        }

                    }
                }
            }
        }

        avgSeatDiff=avgSeatDiff/(count==0?1:count+1);
        transCount=count;
        Bundle bundle=new Bundle();
        bundle.putSerializable("sameList", (Serializable) sameList);
        bundle.putDouble("transCount",transCount);
        bundle.putDouble("avgSeatDiff",avgSeatDiff);
        //return count * 2;
        return bundle;
    }

    public List<TransportationEntity> parseDateFormServer(Response response) {
        List<TransportationEntity> serverDataList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        try {
            String body = response.body().string();
            Log.e("TransportationEntity", body);
            JSONArray array = new JSONArray(body);

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String type = jsonObject.getString("type");
                String No = jsonObject.getString("no");
                Integer seat = Integer.parseInt(jsonObject.getString("seat").equals("null") ? "-1" : jsonObject.getString("seat"));
                String date = jsonObject.getString("date");
                Date parsedDate = sdf.parse(date);
                serverDataList.add(new TransportationEntity(type, No, seat, parsedDate));

            }
        } catch (JSONException | ParseException | IOException e) {
            e.printStackTrace();
        }
        return serverDataList;
    }
}

package com.ecnu.traceability.judge;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TransportationJudge {
    private DBHelper dbHelper = null;
    private List<TransportationEntity> dataFromServer = null;

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

    public int judge(List<TransportationEntity> patientDataList, List<TransportationEntity> locDataList) {
        //        List<TransportationEntity> dataFromServer =getDataFromServer(patientMacAddress);
        //        List<TransportationEntity> locDataList = getDataFromDatabase();
        int count = 0;
        for (TransportationEntity serData : patientDataList) {
            for (TransportationEntity locData : locDataList) {
                if (serData.getNO().equals(locData.getNO())) {
                    if (DateUtils.dataDiff(serData.getDate(), locData.getDate()) < 200) {
                        count++;
                    }
                }
            }
        }

        return count * 2;
    }

    public List<TransportationEntity> parseDateFormServer(Response response) {
        List<TransportationEntity> serverDataList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


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

package com.ecnu.traceability.judge;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.DateUtils;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.io.IOException;
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


    public TransportationJudge(String PatientMac){
        HTTPUtils.queryPatientTransportationinfo(PatientMac, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public void getDataFromServer(String patientMacAddress) {
//        String url="";//网址
//        HTTPUtils.getDataFromServer("", new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//              dataFromServer=
//            }
//        =
    }

    public List<TransportationEntity> getDataFromDatabase() {
        List<TransportationEntity> list = dbHelper.getSession().getTransportationEntityDao().loadAll();
        return list;
    }


    public int judge(String patientMacAddress) {
//        List<TransportationEntity> dataFromServer =getDataFromServer(patientMacAddress);
        int count = 0;
        List<TransportationEntity> locDataList = getDataFromDatabase();
        for (TransportationEntity serData : dataFromServer) {
            for (TransportationEntity locData : locDataList) {
                if (serData.getNO().equals(locData.getNO())) {
                    if (DateUtils.dataDiff(serData.getDate(), locData.getDate()) < 200) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
}

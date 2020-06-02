package com.ecnu.traceability.judge;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.DateUtils;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.util.List;

public class TransportationJudge {
    private DBHelper dbHelper = null;
    private List<TransportationEntity> dataFromServer = null;

    public TransportationJudge(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void getDataFromServer() {
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


    public int judge() {
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

package com.ecnu.traceability;

import android.os.Bundle;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisUtil;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntityDao;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfoToOneNet {

    private DBHelper dbHelper = DBHelper.getInstance();
    private static final String TAG = "InfoToOneNet";


    public InfoToOneNet(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void pushLocationMapData(Map<String, Integer> locationMap) {
        JSONArray datapoints = processLocationMapRawData(locationMap);
        sendLocationDateToServer(datapoints);
    }
    public void pushReportAndpersonCountData(List<ReportInfoEntity> reportInfoList) {
        JSONArray datapoints = prepareReportData(reportInfoList);
        datapoints=processCountRawData(datapoints);
        sendReportInfoToOneNet(datapoints);
    }

    public void pushMapDateToOneNet( List<LocationEntity> locationList) {
//        List<LocationEntity> locationList = dbHelper.getSession().getLocationEntityDao().queryBuilder().orderAsc(LocationEntityDao.Properties.Date).list();
//        HTTPUtils.addLocationInfoList(locationList);
        String deviceId = "601016239";
        String datastream = "data_flow_1";
        JSONArray datapoints = new JSONArray();
        try {
            for (LocationEntity latlon : locationList) {
                JSONObject location = new JSONObject();

                location.putOpt("lat", latlon.getLatitude());
                location.putOpt("lon", latlon.getLongitude());
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", location);
                datapoints.put(datapoint);
            }

            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);
            OneNetDeviceUtils.sendData(deviceId, request);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void sendReportInfoToOneNet(JSONArray datapoints) {
        String deviceId = "601016239";
        String datastream = "data_flow_2";
        try {
            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);
            OneNetDeviceUtils.sendData(deviceId, request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray processCountRawData(JSONArray datapoints) {

        BluetoothAnalysisUtil util = new BluetoothAnalysisUtil(dbHelper);
        Bundle bundle = util.processData();
        Integer[] ans = (Integer[]) bundle.get("countMap");
        ArrayList dateList = (ArrayList) bundle.get("dateList");

        try {
            for (int i = 0; i < dateList.size(); i++) {
                JSONObject numCount = new JSONObject();
                numCount.putOpt("x", dateList.get(i));
                numCount.putOpt("y1", ans[i]);
                numCount.putOpt("flag", 2);
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", numCount);
                datapoints.put(datapoint);
//                Log.e("数据", String.valueOf(datapoints));
//                Log.e("数据：", String.valueOf(ans[i]));
            }

            return datapoints;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public JSONArray processLocationMapRawData(Map<String, Integer> locationMap) {
        JSONArray datapoints = new JSONArray();
        try {
            for (Map.Entry<String, Integer> entry : locationMap.entrySet()) {
                Log.e("data", entry.getKey());
                JSONObject location = new JSONObject();
                location.putOpt("value", entry.getValue());
                location.putOpt("name", entry.getKey());
                location.putOpt("color", getColor());

                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", location);
                datapoints.put(datapoint);
            }

            return datapoints;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //随机生成颜色代码
    public String getColor() {
        //红色
        String red;
        //绿色
        String green;
        //蓝色
        String blue;
        //生成随机对象
        Random random = new Random();
        //生成红色颜色代码
        red = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成绿色颜色代码
        green = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成蓝色颜色代码
        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();

        //判断红色代码的位数
        red = red.length() == 1 ? "0" + red : red;
        //判断绿色代码的位数
        green = green.length() == 1 ? "0" + green : green;
        //判断蓝色代码的位数
        blue = blue.length() == 1 ? "0" + blue : blue;
        //生成十六进制颜色值
        String color = "#" + red + green + blue;
        return color;
    }


    public void sendLocationDateToServer(JSONArray datapoints) {

        String deviceId = "601016239";
        String datastream = "data_flow_3";

        try {
            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);
            OneNetDeviceUtils.sendData(deviceId, request);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONArray prepareReportData(List<ReportInfoEntity> reportInfoList) {
//        List<ReportInfoEntity> reportInfoList = dbHelper.getSession().getReportInfoEntityDao().queryBuilder()
//                .orderAsc(ReportInfoEntityDao.Properties.Date).list();
        JSONArray datapoints = new JSONArray();
        try {
            for (ReportInfoEntity reportFromDB : reportInfoList) {

                SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Log.e(TAG, sfd.format(reportFromDB.getDate()));
                JSONObject reportInfo = new JSONObject();
                reportInfo.put("MacAddress", MacAddress.getBtAddressByReflection());
                reportInfo.put("Description", reportFromDB.getText());
                reportInfo.put("Date", sfd.format(reportFromDB.getDate()));
                reportInfo.put("flag", 1);

                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", reportInfo);
                datapoints.put(datapoint);
            }

            return datapoints;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}

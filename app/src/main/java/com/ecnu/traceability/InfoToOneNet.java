package com.ecnu.traceability;

import android.os.Bundle;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisUtil;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.model.LatLonPoint;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class InfoToOneNet {

    private DBHelper dbHelper = DBHelper.getInstance();
    private static final String TAG = "InfoToOneNet";
    public static String deviceId;
    private MqttUtil mqttUtil = MqttUtil.getInstance();

    public InfoToOneNet(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
        List<LocalDevice> devices=dbHelper.getSession().getLocalDeviceDao().loadAll();
        if(null!=devices&&devices.size()>0){
            LocalDevice device=devices.get(0);
            this.deviceId=device.getDeviceId();
        }else{
            this.deviceId="610475801";
        }
        //this.deviceId="598587076";
    }

    public void pushRealTimeLocation(LatLonPoint latLonPoint, Date date) {
        //String deviceId = "601016239";
        String datastream = "data_flow_for_real_time_loc";

        JSONObject request = new JSONObject();

        try {
            JSONObject location = new JSONObject();
            location.putOpt("lat", latLonPoint.getLatitude());
            location.putOpt("lon", latLonPoint.getLongitude());
            location.putOpt("date", date);

            JSONObject datapoint = new JSONObject();
            datapoint.putOpt("value", location);
            JSONArray jsonArray=new JSONArray();
            jsonArray.put(datapoint);
            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", jsonArray);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);
            request.putOpt("datastreams", datastreams);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OneNetDeviceUtils.sendData(deviceId, request);

    }

    public void pushTransportData(List<TransportationEntity> transportationEntityList){
        String datastream = "data_flow_for_transportation";

        JSONArray datapoints=new JSONArray();
        String mac=OneNetDeviceUtils.macAddress;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            for (TransportationEntity trans : transportationEntityList) {
                JSONObject transObj=new JSONObject();
                transObj.putOpt("mac",mac);
                transObj.putOpt("type",trans.getType());
                transObj.putOpt("no",trans.getNO());
                transObj.putOpt("seat",trans.getSeat());
                transObj.putOpt("date",sdf.format(trans.getDate()));
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", transObj);
                datapoints.put(datapoint);
            }

            mqttUtil.publish("transportation",datapoints.toString());

            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);
            OneNetDeviceUtils.sendData(deviceId, request);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
    public void pushBarChartData(){
        String datastream = "data_flow_for_bar_chart";

        JSONArray datapoints=new JSONArray();
        BluetoothAnalysisUtil util = new BluetoothAnalysisUtil(dbHelper);
        Bundle bundle = util.processData();
        Integer[] ans = (Integer[]) bundle.get("countMap");
        ArrayList dateList = (ArrayList) bundle.get("dateList");

        try {
            for (int i = 0; i < dateList.size(); i++) {
                JSONObject numCount = new JSONObject();
                numCount.putOpt("x", dateList.get(i));
                numCount.putOpt("y1", ans[i]);
                //numCount.putOpt("flag", 2);
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", numCount);
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
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }

    public void pushMapDateToOneNet(List<LocationEntity> locationList) {
        String datastream = "data_flow_for_map";
        JSONArray datapoints = new JSONArray();
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            for (LocationEntity latlon : locationList) {
                JSONObject location = new JSONObject();
                location.putOpt("mac",OneNetDeviceUtils.macAddress);
                location.putOpt("lat", latlon.getLatitude());
                location.putOpt("lon", latlon.getLongitude());
                location.putOpt("date",sfd.format(latlon.getDate()));
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", location);
                datapoints.put(datapoint);
            }

            mqttUtil.publish("location",datapoints.toString());

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

    public void sendReportInfoToOneNet(List<ReportInfoEntity> reportInfoList) {
        //String deviceId = "601016239";
        String datastream = "data_flow_for_board";
        JSONArray datapoints = new JSONArray();
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            for (ReportInfoEntity reportFromDB : reportInfoList) {

                Log.e(TAG, sfd.format(reportFromDB.getDate()));
                JSONObject reportInfo = new JSONObject();
                reportInfo.put("mac", OneNetDeviceUtils.macAddress);
                reportInfo.put("Description", reportFromDB.getText());
                reportInfo.put("Date", sfd.format(reportFromDB.getDate()));
                //reportInfo.put("flag", 1);

                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", reportInfo);
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

    public void sendPieChartData(Map<String, Integer> locationMap) {

        String datastream = "data_flow_for_pie_chart";
        JSONArray datapoints = new JSONArray();

        try {
            for (Map.Entry<String, Integer> entry : locationMap.entrySet()) {
                Log.e("data", entry.getKey());
                JSONObject location = new JSONObject();
                location.putOpt("value", entry.getValue());
                location.putOpt("name", entry.getKey());
                location.putOpt("color", GeneralUtils.getColor());

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

        } catch (
                JSONException e) {
            e.printStackTrace();
        }

    }



}

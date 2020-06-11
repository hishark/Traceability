package com.ecnu.traceability.Utils;

import android.util.Log;

import com.chinamobile.iot.onenet.http.HttpExecutor;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HTTPUtils {
    private static final String IP = "192.168.1.6";
    public static final String TAG = "HTTPUtils";
    private static HttpExecutor httpExecutor = new HttpExecutor(new OkHttpClient());

    public static void getDataFromServer(String url, Callback callback) {
        httpExecutor.get(url, callback);
    }

    public static void sendByOKHttp(final String url, RequestBody data, Callback callback) {
        httpExecutor.post(url, data, callback);

    }

    public static void addTelephone(String tel) {
        String url = "http://" + IP + ":8080/TraceabilityServer/sendTel";
        JSONObject requestContent = new JSONObject();

        try {
            requestContent.put("cur_telephone", tel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());

        sendByOKHttp(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public static void addUser(LocalDevice device) {
        String url = "http://" + IP + ":8080/TraceabilityServer/user/add";
        JSONObject requestContent = new JSONObject();

        try {
            requestContent.put("macAddress", device.getMac());
            requestContent.put("deviceId", device.getDeviceId());
            requestContent.put("flag", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());

        sendByOKHttp(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public static void addLocationInfoList(List<LocationEntity> locationEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addLocationInfo";

//        getDataFromServer("http://192.168.1.6:8080/TraceabilityServer/test", new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                //获取数据
//                Log.e("test",response.body().string());
//                Log.e("test", String.valueOf(response));
//                Log.e("test", "成功");
//
//            }
//        });
        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;
        SimpleDateFormat sd = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            for (LocationEntity location : locationEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("latitude", location.getLatitude());
                obj.put("longitude", location.getLongitude());
                Date date=sd.parse(location.getDate().toString());
                obj.put("date",sdf.format(date));
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("request", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addLocationInfoList", String.valueOf(e));
                    Log.e("addLocationInfoList", "失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addLocationInfoList", String.valueOf(response));
                    Log.e("addLocationInfoList", "成功");
                }
            });
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

    }


    public void addReportInfoList(List<ReportInfoEntity> reportInfoEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addReportInfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;

        try {
            for (ReportInfoEntity report : reportInfoEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("description", report.getText());
                obj.put("date", report.getDate());
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addLocationInfoList", String.valueOf(e));
                    Log.e("addLocationInfoList", "失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addLocationInfoList", String.valueOf(response));
                    Log.e("addLocationInfoList", "成功");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void addTransportationinfo(List<TransportationEntity> transportationEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addTransportationinfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;

        try {
            for (TransportationEntity transportation : transportationEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("No", transportation.getNO());
                obj.put("seat", transportation.getSeat());
                obj.put("type", transportation.getType());
                obj.put("date", transportation.getDate());
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addLocationInfoList", String.valueOf(e));
                    Log.e("addLocationInfoList", "失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addLocationInfoList", String.valueOf(response));
                    Log.e("addLocationInfoList", "成功");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPatientMacAddress(Callback callback) {
        String url = "http://" + IP + ":8080/TraceabilityServer/getPatientData";
        getDataFromServer(url, callback);
    }

    public void queryPatientLocationInfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getPatientData" + macAddress + "/" + patientMac;
        getDataFromServer(url, callback);
    }

    public void queryPatientReportInfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getReportInfo" + macAddress + "/" + patientMac;
        getDataFromServer(url, callback);
    }

    public void queryPatientTransportationinfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getTransportationinfo" + macAddress + "/" + patientMac;
        getDataFromServer(url, callback);
    }
}

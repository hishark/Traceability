package com.ecnu.traceability.Utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.chinamobile.iot.onenet.http.HttpExecutor;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.data_analyze.BluetoothAnalysisUtil;
import com.ecnu.traceability.information_reporting.Dao.ReportInfoEntity;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.machine_learning.TrainModel;
import com.ecnu.traceability.model.LatLonPoint;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class HTTPUtils {
    //    private static final String IP = "132.232.144.76";
    //    private static final String IP = "192.168.1.10";
    private static final String IP = "192.168.43.242";
    public static final String TAG = "HTTPUtils";
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)//设置连接超时时间
            .readTimeout(120, TimeUnit.SECONDS)//设置读取超时时间
            .build();
    private static HttpExecutor httpExecutor = new HttpExecutor(client);
    private static WebSocket mSocket;

    public static void getDataFromServer(String url, Callback callback) {
        httpExecutor.get(url, callback);
    }

    public static void sendByOKHttp(final String url, RequestBody data, Callback callback) {
        httpExecutor.post(url, data, callback);
    }

    public static void addTelephone(String tel) {
        String macAddress = OneNetDeviceUtils.macAddress;

        String url = "http://" + IP + ":8080/TraceabilityServer/tel/add";
        JSONObject requestContent = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            requestContent.put("telephone", tel);
            requestContent.put("macAddress", macAddress);
            requestContent.put("flag", "false");
            requestContent.put("date", sdf.format(new Date()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());

        Log.i("addTelephone", String.valueOf(requestContent));
        sendByOKHttp(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public static void addUser(LocalDevice device, DBHelper dbHelper) {
        String url = "http://" + IP + ":8080/TraceabilityServer/user/add";
        JSONObject requestContent = new JSONObject();

        try {
            requestContent.put("macAddress", device.getMac());
            requestContent.put("deviceId", device.getDeviceId());
            requestContent.put("tel", "null");
            requestContent.put("flag", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
        Log.e("addUser", requestContent.toString());
        sendByOKHttp(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String deviceId = response.body().string();
                    Log.e(TAG, deviceId);
                    device.setDeviceId(deviceId);
                    dbHelper.getSession().getLocalDeviceDao().insert(device);
                }
            }
        });
    }

    public static void addLocationInfoList(List<LocationEntity> locationEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addLocationInfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;
        SimpleDateFormat sd = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        sd.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            for (LocationEntity location : locationEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("latitude", location.getLatitude());
                obj.put("longitude", location.getLongitude());
                Date date = sd.parse(location.getDate().toString());
                obj.put("date", sdf.format(date));
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addLocationInfoList", requestContent.toString());
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

    public static void addPushedInfo(String patientMacAddress) {
        String url = "http://" + IP + ":8080/TraceabilityServer/pushedinfo/add";
        String macAddress = OneNetDeviceUtils.macAddress;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject requestContent = new JSONObject();

        try {
            requestContent.put("patientMacAddress", patientMacAddress);
            requestContent.put("userMacAddress", macAddress);
            requestContent.put("date", sdf.format(new Date()));
            requestContent.put("disease", "aaa");
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addPushedInfo", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addReportInfoList", String.valueOf(e));
                    Log.e("addReportInfoList", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addReportInfoList", String.valueOf(response));
                    Log.e("addReportInfoList", "=================成功=================");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public static void addReportInfoList(List<ReportInfoEntity> reportInfoEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addReportInfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;
        SimpleDateFormat sd = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        sd.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            for (ReportInfoEntity report : reportInfoEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("description", report.getText());
                Date date = sd.parse(report.getDate().toString());
                obj.put("date", sdf.format(date));
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addReportInfoList", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addReportInfoList", String.valueOf(e));
                    Log.e("addReportInfoList", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addReportInfoList", String.valueOf(response));
                    Log.e("addReportInfoList", "=================成功=================");
                }
            });
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }


    public static void addTransportationinfo(List<TransportationEntity> transportationEntityList) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addTransportationinfo";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;
        SimpleDateFormat sd = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        sd.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            for (TransportationEntity transportation : transportationEntityList) {
                JSONObject obj = new JSONObject();
                obj.put("macAddress", macAddress);
                obj.put("No", transportation.getNO());
                obj.put("seat", transportation.getSeat());
                obj.put("type", transportation.getType());
                Date date = sd.parse(transportation.getDate().toString());
                obj.put("date", sdf.format(date));
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addTransportationinfo", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addTransportationinfo", String.valueOf(e));
                    Log.e("addTransportationinfo", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addTransportationinfo", String.valueOf(response));
                    Log.e("addTransportationinfo", "=================成功=================");
                }
            });
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void addAllRelationshipList(List<BluetoothDeviceEntity> bluetoothDeviceEntityList, int flag, boolean isInfection) {
        String url = "http://" + IP + ":8080/TraceabilityServer/relationshipList/add";

        JSONArray jsonArray = new JSONArray();
        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;
        SimpleDateFormat sd = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        sd.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            for (BluetoothDeviceEntity macInfo : bluetoothDeviceEntityList) {
                JSONObject obj = new JSONObject();
                if (isInfection == true) {
                    obj.put("originMac", macAddress);
                    obj.put("targetMac", macInfo.getMacAddress());
                } else {
                    obj.put("originMac", macInfo.getMacAddress());
                    obj.put("targetMac", macAddress);
                }

                Date date = sd.parse(macInfo.getDate().toString());
                obj.put("date", sdf.format(date));
                obj.put("flag", flag);
                jsonArray.put(obj);
            }

            requestContent.put("data", jsonArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addAllRelationship", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addTransportationinfo", String.valueOf(e));
                    Log.e("addTransportationinfo", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addTransportationinfo", String.valueOf(response));
                    Log.e("addTransportationinfo", "=================成功=================");
                }
            });
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }


    public static void pushPieChartData(Map<String, Integer> locationMap) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addPieChartData";
        String macAddress = OneNetDeviceUtils.macAddress;

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

            JSONObject requestContent = new JSONObject();
            requestContent.putOpt("datastreams", datapoints);
            requestContent.putOpt("macAddress", macAddress);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addAllRelationship", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addTransportationinfo", String.valueOf(e));
                    Log.e("addTransportationinfo", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addTransportationinfo", String.valueOf(response));
                    Log.e("addTransportationinfo", "=================成功=================");
                }
            });
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public static void pushRealtimeLocation(LatLonPoint latLonPoint, Date date) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addRealtimeLocation";

        JSONObject requestContent = new JSONObject();
        String macAddress = OneNetDeviceUtils.macAddress;

        try {
            JSONObject location = new JSONObject();
            location.putOpt("lat", latLonPoint.getLatitude());
            location.putOpt("lon", latLonPoint.getLongitude());
            location.putOpt("date", date);

            JSONObject datapoint = new JSONObject();
            datapoint.putOpt("value", location);
//            JSONArray jsonArray = new JSONArray();
//            jsonArray.put(datapoint);
//
//            JSONArray datastreams = new JSONArray();
//            datastreams.put(jsonArray);
            requestContent.putOpt("datastreams", datapoint);
            requestContent.putOpt("macAddress", macAddress);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addAllRelationship", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addTransportationinfo", String.valueOf(e));
                    Log.e("addTransportationinfo", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addTransportationinfo", String.valueOf(response));
                    Log.e("addTransportationinfo", "=================成功=================");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void pushBarChartData(DBHelper dbHelper) {
        String url = "http://" + IP + ":8080/TraceabilityServer/addBarChartData";
        String macAddress = OneNetDeviceUtils.macAddress;

        JSONArray datapoints = new JSONArray();
        BluetoothAnalysisUtil util = new BluetoothAnalysisUtil(dbHelper);
        Bundle bundle = util.processData();
        Integer[] ans = (Integer[]) bundle.get("countMap");
        ArrayList dateList = (ArrayList) bundle.get("dateList");

        try {
            for (int i = 0; i < dateList.size(); i++) {
                JSONObject numCount = new JSONObject();
                numCount.putOpt("x", dateList.get(i));
                numCount.putOpt("y1", ans[i]);
                JSONObject datapoint = new JSONObject();
                datapoint.putOpt("value", numCount);
                datapoints.put(datapoint);
            }
            JSONObject requestContent = new JSONObject();
            requestContent.putOpt("datastreams", datapoints);
            requestContent.putOpt("macAddress", macAddress);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestContent.toString());
            Log.e("addAllRelationship", requestContent.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addTransportationinfo", String.valueOf(e));
                    Log.e("addTransportationinfo", "=================失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addTransportationinfo", String.valueOf(response));
                    Log.e("addTransportationinfo", "=================成功=================");
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void pushIsolateStatus(DBHelper dbHelper, boolean is_in_isolate) {

        String mac = OneNetDeviceUtils.macAddress;
        String deviceId = "610475801";
        List<LocalDevice> devices = dbHelper.getSession().getLocalDeviceDao().loadAll();
        if (null != devices && devices.size() > 0) {
            LocalDevice device = devices.get(0);
            deviceId = device.getDeviceId();
        }
        String url = "http://" + IP + ":8080/TraceabilityServer/addIsolateStates";
        JSONObject stateObj = new JSONObject();
        try {
            stateObj.put("deviceId", deviceId);
            stateObj.put("mac", mac);
            stateObj.put("state", is_in_isolate);
//            JSONObject requestContent = new JSONObject();
//            requestContent.putOpt("datastreams", stateObj);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), stateObj.toString());
            Log.e("pushIsolateStatus", stateObj.toString());
            sendByOKHttp(url, requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("addTransportationinfo", String.valueOf(e));
                    Log.e("addTransportationinfo", "=================上传隔离状态失败=================");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("addTransportationinfo", String.valueOf(response));
                    Log.e("addTransportationinfo", "=================上传隔离状态成功=================");
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void getPatientMacAddress(Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getPatientData/" + macAddress;
        getDataFromServer(url, callback);
    }

    public static void queryPatientLocationInfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getLocationInfo/" + macAddress + "/" + patientMac;
        Log.e("queryLocationURL", url);
        getDataFromServer(url, callback);
    }

    public static void queryPatientReportInfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getReportInfo/" + macAddress + "/" + patientMac;
        Log.e("queryreportURL", url);

        getDataFromServer(url, callback);
    }

    public static void queryPatientTransportationinfo(String patientMac, Callback callback) {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/getTransportationinfo/" + macAddress + "/" + patientMac;
        Log.e("queryTransURL", url);

        getDataFromServer(url, callback);
    }


    public static void uploadInfoToServer(List<LocationEntity> locationList,
                                          List<ReportInfoEntity> reportInfoEntityList,
                                          List<TransportationEntity> transportationEntityList) {
        addLocationInfoList(locationList);
        addReportInfoList(reportInfoEntityList);
        addTransportationinfo(transportationEntityList);
    }


    public static ResponseBody upload(File file) throws Exception {
        String macAddress = OneNetDeviceUtils.macAddress;
        String url = "http://" + IP + ":8080/TraceabilityServer/upload/model/" + macAddress;
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .build();


        Request request = new Request.Builder()
//                .header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        return response.body();
    }

    public static void download() {

        String url = "http://" + IP + ":8080/TraceabilityServer/download/model";
        DownloadUtil.get().download(url, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                Log.i(TAG, "模型下载成功");
            }

            @Override
            public void onDownloading(int progress) {
//                if (progress % 10 == 0)
//                    Log.i(TAG, String.valueOf(progress));
            }

            @Override
            public void onDownloadFailed() {
                Log.i(TAG, "模型下载失败");
            }
        });
    }

    private static final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            mSocket = webSocket;
            String macAddress = OneNetDeviceUtils.macAddress;

            //连接成功后，发送登录信息
            String message = "{\"type\":\"login\",\"user_id\":\"" + macAddress + "\"}";
            mSocket.send(message);
            Log.i(TAG, "连接成功！");

        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            Log.i(TAG, "receive bytes:" + bytes.hex());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.i(TAG, "receive text:" + text);
            String macAddress = OneNetDeviceUtils.macAddress;
            //收到服务器端发送来的信息后，每隔25秒发送一次心跳包
            String message = "{\"type\":\"heartbeat\",\"user_id\":\"" + macAddress + "\"}";

            // final String message = "{\"type\":\"heartbeat\",\"user_id\":\"heartbeat\"}";
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSocket.send(message);
                }
            }, 25000);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
//            websocketConnect();
            Log.i(TAG, "closed:" + reason);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
//            super.onClosing(webSocket, code, reason);
            Log.i(TAG, "closing:" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
//            websocketConnect();
            Log.i(TAG, "failure:" + t.getMessage());
        }
    }

    public static void websocketConnect() {
        //这里不知道为什么要重新开一个，否则会出现java.io.InterruptedIOException: executor rejected异常
        //        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
        //                .readTimeout(3, TimeUnit.SECONDS)//设置读取超时时间
        //                .writeTimeout(3, TimeUnit.SECONDS)//设置写的超时时间
        //                .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时时间
        //                .build();

        String url = "http://" + IP + ":8080/TraceabilityServer/websocket";
        Request request = new Request.Builder().url(url).build();
        EchoWebSocketListener socketListener = new EchoWebSocketListener();
        client.newWebSocket(request, socketListener);
        //        client.dispatcher().executorService().shutdown();
        //        client.dispatcher().executorService().shutdown();   //清除并关闭线程池
        //        client.connectionPool().evictAll();                 //清除并关闭连接池
        //        client.cache().close();                             //清除cache
        //        mOkHttpClient.newWebSocket(request, socketListener);
        //        mOkHttpClient.dispatcher().executorService().shutdown();
    }
}

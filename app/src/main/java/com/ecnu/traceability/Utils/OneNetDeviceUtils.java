package com.ecnu.traceability.Utils;

import android.content.Context;
import android.util.Log;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.model.DeviceItem;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.model.LocalDeviceDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneNetDeviceUtils {
    private static final String TAG = "OneNetDeviceUtils";
    public static String macAddress;

    public static boolean isExistsDevice(DBHelper dbHelper, String mac) {
        Log.e("mac", mac);
        List<LocalDevice> deviceList = dbHelper.getSession().getLocalDeviceDao().queryBuilder().where(LocalDeviceDao.Properties.Mac.eq(mac)).list();
        if (deviceList.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static void addDevice(DBHelper dbHelper, Context context) {
        JSONObject requestContent = new JSONObject();
        String mac = MacAddress.getBluetoothMAC(context);
        macAddress = mac;
        if (null != mac) {
            //List<LocalDevice> deviceList = dbHelper.getSession().getLocalDeviceDao().queryBuilder().where(LocalDeviceDao.Properties.Mac.eq(mac)).list();
            if (!isExistsDevice(dbHelper, mac)) {//该用户的本地数据库中无记录
                try {
                    //设备名mac地址
                    requestContent.putOpt("title", mac);
                    //协议
                    requestContent.putOpt("protocol", "HTTP");
                    //数据保密性
                    requestContent.putOpt("private", false);
                    //鉴权信息
                    requestContent.putOpt("auth_info", mac);

                    OneNetApi.addDevice(requestContent.toString(), new OneNetApiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            JsonObject resp = new JsonParser().parse(response).getAsJsonObject();
                            int errno = resp.get("errno").getAsInt();

                            if (0 == errno) {
                                JsonObject jsobj = (JsonObject) resp.get("data");
                                String deviceId = String.valueOf(jsobj.get("device_id"));
                                LocalDevice device = new LocalDevice(mac, deviceId);
                                dbHelper.getSession().getLocalDeviceDao().insert(device);
                                //成功
                                HTTPUtils.addUser(device);

                                Log.e("OneNetDeviceUtils", String.valueOf(resp.get("data.device_id")));
                                //{"errno":0,"data":{"device_id":"602595381"},"error":"succ"}
                                Log.e("OneNetDeviceUtils", "+=============成功添加设备===========+");
                            } else {
                                //未成功
                                Log.e("OneNetDeviceUtils", "+=============添加设备失败===========+");
                                String error = resp.get("error").getAsString();
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e("Fatal err:", "mac 地址无法获取");
        }
    }

    public static void getDevices(Context context, DBHelper dbHelper) {
        String mac = MacAddress.getBluetoothMAC(context);
        macAddress = mac;
        Map<String, String> urlParams = new HashMap<>();
        OneNetApi.fuzzyQueryDevices(urlParams, new OneNetApiCallback() {
            @Override
            public void onSuccess(String response) {
                JsonObject resp = new JsonParser().parse(response).getAsJsonObject();
                int errno = resp.get("errno").getAsInt();
                if (0 == errno) {
                    parseData(resp.get("data").getAsJsonObject(), context, dbHelper, mac);
                } else {
                    addDevice(dbHelper, context);
                    String error = resp.get("error").getAsString();
                    //Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void parseData(JsonObject data, Context context, DBHelper dbHelper, String mac) {
        if (null == data) {
            return;
        }
        boolean flag = false;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DeviceItem.class, new DeviceItemDeserializer());
        Gson gson = gsonBuilder.create();
        JsonArray jsonArray = data.get("devices").getAsJsonArray();
        Log.e("data", String.valueOf(data.get("devices")));
        for (JsonElement element : jsonArray) {
            DeviceItem device = gson.fromJson(element, DeviceItem.class);
            if (device.getTitle().equals(mac) && !isExistsDevice(dbHelper, mac)) {
                LocalDevice locDevice = new LocalDevice(mac, device.getId());
                dbHelper.getSession().getLocalDeviceDao().insert(locDevice);
                flag = true;
            }
        }
        if (!flag) {
            addDevice(dbHelper, context);
        }
    }


    public static void sendData(String deviceId, JSONObject data) {

        OneNetApi.addDataPoints(deviceId, data.toString(), new OneNetApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.e(TAG, response);
                Log.e(TAG, "=============信息发送成功=============");
                //GeneralUtils.showToastInService(this,"信息发送成功");
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Log.e(TAG, "=============信息发送失败=============");
            }
        });
    }
}

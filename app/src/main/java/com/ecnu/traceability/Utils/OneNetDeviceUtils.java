package com.ecnu.traceability.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.model.DeviceItem;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.model.LocalDeviceDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;

import static com.amap.api.maps.model.BitmapDescriptorFactory.getContext;

public class OneNetDeviceUtils {
    private static final String TAG = "OneNetDeviceUtils";

    public static void addDevice(DBHelper dbHelper, Context context) {
        JSONObject requestContent = new JSONObject();
        String mac = MacAddress.getBluetoothMAC(context);

        if (null != mac) {
            List<LocalDevice> deviceList = dbHelper.getSession().getLocalDeviceDao().queryBuilder().where(LocalDeviceDao.Properties.Mac.eq(mac)).list();
            if (deviceList.size() == 0) {
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
                                JsonObject jsobj= (JsonObject) resp.get("data");
                                String deviceId= String.valueOf(jsobj.get("device_id"));
                                dbHelper.getSession().getLocalDeviceDao().insert(new LocalDevice(mac,deviceId));
                                //成功
                                Log.e("OneNetDeviceUtils", String.valueOf(resp.get("data.device_id")));
//                                {"errno":0,"data":{"device_id":"602595381"},"error":"succ"}
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

    private void getDevices(final boolean loadMore) {
//        if (loadMore) {
//            mCurrentPage++;
//        } else {
//            mCurrentPage = 1;
//        }
        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put("page", String.valueOf(mCurrentPage));
//        urlParams.put("per_page", "10");
        OneNetApi.fuzzyQueryDevices(urlParams, new OneNetApiCallback() {
            @Override
            public void onSuccess(String response) {
//                mSwipeRefreshLayout.setRefreshing(false);
                JsonObject resp = new JsonParser().parse(response).getAsJsonObject();
                int errno = resp.get("errno").getAsInt();
                if (0 == errno) {
                    parseData(resp.get("data").getAsJsonObject(), loadMore);
                } else {
                    String error = resp.get("error").getAsString();
//                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
//                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void parseData(JsonObject data, boolean loadMore) {
        if (null == data) {
            return;
        }
        int mTotalCount = data.get("total_count").getAsInt();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DeviceItem.class, new DeviceItemDeserializer());
        Gson gson = gsonBuilder.create();
        JsonArray jsonArray = data.get("devices").getAsJsonArray();
        List<DeviceItem> devices = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            devices.add(gson.fromJson(element, DeviceItem.class));
        }
//        if (!loadMore) {
//             mDeviceItems.clear();
//        }
//        mDeviceItems.addAll(devices);
//        mAdapter.setNewData(mDeviceItems);
    }


    public static void sendData(String deviceId, JSONObject data) {

        OneNetApi.addDataPoints(deviceId, data.toString(), new OneNetApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.e(TAG, response);
                Log.e(TAG, "=============信息发送成功=============");
//                GeneralUtils.showToastInService(this,"信息发送成功");
            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Log.e(TAG, "=============信息发送失败=============");
            }
        });
    }
}

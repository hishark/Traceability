package com.ecnu.traceability.Utils;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.location.Dao.LocationEntityDao;
import com.ecnu.traceability.model.DeviceItem;
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

public class OneNetDeviceUtils {
    private static final String TAG = "OneNetDeviceUtils";

    public static void addDevice() {
        JSONObject requestContent = new JSONObject();
        try {
            //设备名mac地址
            String mac = MacAddress.getBtAddressByReflection();
            requestContent.putOpt("title", mac);
            //协议
            requestContent.putOpt("protocol", "HTTP");
            //数据保密性
            requestContent.putOpt("private", false);
            //鉴权信息
            requestContent.putOpt("auth_info", "123456789");

            OneNetApi.addDevice(requestContent.toString(), new OneNetApiCallback() {
                @Override
                public void onSuccess(String response) {
                    JsonObject resp = new JsonParser().parse(response).getAsJsonObject();
                    int errno = resp.get("errno").getAsInt();
                    if (0 == errno) {
                        //成功
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
                Log.e(TAG, "=============发送成功=============");
                Log.e(TAG, "=============发送成功=============");
                Log.e(TAG, "=============发送成功=============");
                Log.e(TAG, "=============发送成功=============");
                Log.e(TAG, "=============发送成功=============");

            }

            @Override
            public void onFailed(Exception e) {
                e.printStackTrace();
                Log.e(TAG, "=============发送失败=============");
                Log.e(TAG, "=============发送失败=============");
                Log.e(TAG, "=============发送失败=============");
                Log.e(TAG, "=============发送失败=============");

            }
        });
    }
}

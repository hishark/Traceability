package com.ecnu.traceability.Utils;

import android.util.Log;

import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.chinamobile.iot.onenet.http.HttpExecutor;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HTTPUtils {
    public static final String TAG="HTTPUtils";
    private static HttpExecutor httpExecutor=new HttpExecutor(new OkHttpClient());

    public  static void getDataFromServer(String url, Callback callback){
        httpExecutor.get(url,callback);
    }

    private static void sendByOKHttp(final String url, RequestBody data, Callback callback) {
        httpExecutor.post(url,data,callback);

    }
}

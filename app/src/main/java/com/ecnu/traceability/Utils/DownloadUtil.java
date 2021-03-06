package com.ecnu.traceability.Utils;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ecnu.traceability.machine_learning.TrainModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil {

    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * @param url      下载连接
     * @param listener 下载监听
     */
    public void download(final String url, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                listener.onDownloadFailed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                InputStream is;
                long total = response.body().contentLength();
                //Log.i("sum_num", String.valueOf(total));
                is = response.body().byteStream();
                FileOutputStream fos;
                File ff = new File(TrainModel.dir, "updatedModel.zip");
                if (!ff.exists()) {
                    ff.createNewFile();
                }
                fos = new FileOutputStream(ff);
                int len = 0;
                byte[] bytes = new byte[1024];
                //int sum = 0;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                    //sum+=len;
                    //int progress = (int) (sum * 1.0f / total * 100);
                    // 下载中
                    //listener.onDownloading(progress);
                }
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
//                InputStream is = null;
//                byte[] buf = new byte[2048];
//                int len = 0;
//                FileOutputStream fos = null;
//                // 储存下载文件的目录
//                String savePath = isExistDir(TrainModel.dir.getPath());
//                try {
//                    is = response.body().byteStream();
//                    long total = response.body().contentLength();
//                    File file = new File(TrainModel.dir, "updatedModel.zip");
//                    fos = new FileOutputStream(file);
//                    long sum = 0;
//                    while ((len = is.read(buf)) != -1) {
//                        fos.write(buf, 0, len);
//                        sum += len;
//                        int progress = (int) (sum * 1.0f / total * 100);
//                        // 下载中
//                        listener.onDownloading(progress);
//                    }
//                    fos.flush();
//                    // 下载完成
//                    listener.onDownloadSuccess();
//                } catch (Exception e) {
//                    listener.onDownloadFailed();
//                } finally {
//                    try {
//                        if (is != null)
//                            is.close();
//                    } catch (IOException e) {
//                    }
//                    try {
//                        if (fos != null)
//                            fos.close();
//                    } catch (IOException e) {
//                    }
//                }
            }
        });
    }

    /**
     * @param saveDir
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(Environment.getExternalStorageDirectory(), saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }
}
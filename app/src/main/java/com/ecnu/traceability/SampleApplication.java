package com.ecnu.traceability;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.http.Config;
import com.ecnu.traceability.Utils.MigrationHelper;
import com.ecnu.traceability.Utils.Preferences;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.model.LocalDeviceDao;

import org.greenrobot.greendao.database.StandardDatabase;

import java.util.concurrent.TimeUnit;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化SDK（必须）
        Config config = Config.newBuilder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .retryCount(2)
                .build();
        OneNetApi.init(this, true, config);

        String savedApiKey = Preferences.getInstance(this).getString(Preferences.API_KEY, null);
        if (!TextUtils.isEmpty(savedApiKey)) {
            OneNetApi.setAppKey(savedApiKey);
        }
    }
}

package com.ecnu.traceability.data_analyze;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartView;
import com.ecnu.traceability.AAChartCoreLib.AAChartEnum.AAChartType;
import com.ecnu.traceability.AAChartCoreLib.AAOptionsModel.AADataLabels;
import com.ecnu.traceability.AAChartCoreLib.AAOptionsModel.AAPie;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;

import java.util.Map;

public class LocationAnalysisActivity extends AppCompatActivity {
    private DBHelper dbHelper = DBHelper.getInstance();
    private AAChartView aaChartView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("访问城市统计");

        setContentView(R.layout.activity_location_analysis);
        aaChartView = findViewById(R.id.AAChartView_location);

        Intent intent = new Intent(this, LocationAnalysisService.class);
        bindService(intent, mMessengerConnection, BIND_AUTO_CREATE);
    }


    private static final int MSG_ID_CLIENT = 1;
    private static final int MSG_ID_SERVER = 2;
    private static final String MSG_CONTENT = "getAddress";
    /**
     * 客户端的 Messenger
     */
    Messenger mClientMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg != null && msg.arg1 == MSG_ID_SERVER) {
                if (msg.getData() == null) {
                    return;
                }
                Map<String, Integer> locationMap= (Map<String, Integer>) msg.getData().get(MSG_CONTENT);
                Log.e("IPC", "Message from server: " + locationMap.size());
                prepareShow(locationMap);
            }
        }
    });

    //服务端的 Messenger
    private Messenger mServerMessenger;

    private ServiceConnection mMessengerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mServerMessenger = new Messenger(service);

            Message message = Message.obtain();
            message.arg1 = MSG_ID_CLIENT;
            Bundle bundle = new Bundle();
            bundle.putString(MSG_CONTENT, "测试信息");
            message.setData(bundle);
            message.replyTo = mClientMessenger;     //指定回信人是客户端定义的

            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mServerMessenger = null;
        }
    };


    public void showChart(Object[][] data) {
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Pie)
                .backgroundColor("#ffffff")
                .title("14天内到访地点")
                .subtitle("")
                .dataLabelsFontSize(15f)
                .titleFontSize(20f)
                .dataLabelsFontSize(15f)
                .dataLabelsEnabled(true)//是否直接显示扇形图数据
                .yAxisTitle("℃")
                .series(new AAPie[]{
                        new AAPie()
                                .name("停留时间（小时）")
                                .innerSize("20%")
                                .size(300f)
                                .dataLabels(new AADataLabels()
                                        .enabled(true)
                                        .useHTML(true)
                                        .distance(5f)
                                        .format("<b>{point.name}</b>: <br> {point.percentage:.1f} %"))
                                .data(data),
                });
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel);
    }

    public void prepareShow(Map<String, Integer> locationMap){
//        stopService(new Intent(getBaseContext(), LocationAnalysisService.class));
        Object[][] data = processRawData(locationMap);
        showChart(data);
    }

    public Object[][] processRawData(Map<String, Integer> locationMap) {

        Object[][] data = new Object[locationMap.size()][2];
        int index = 0;
        for (Map.Entry<String, Integer> entry : locationMap.entrySet()) {
            data[index][0] = entry.getKey();
            data[index][1] = entry.getValue();
            index++;
        }
        return data;
    }


}
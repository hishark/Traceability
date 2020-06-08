package com.ecnu.traceability.data_analyze;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartView;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AASeriesElement;
import com.ecnu.traceability.AAChartCoreLib.AAChartEnum.AAChartType;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntityDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothAnalysisActivity extends AppCompatActivity {
    private DBHelper dbHelper = DBHelper.getInstance();
    BluetoothDeviceEntityDao bluetoothDeviceEntityDao;
    List<BluetoothDeviceEntity> deviceEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_analysis);
        setTitle("接触统计");
        // 接触人数统计v1.0  -  当前设备每天碰到了多少个不同的蓝牙设备 - 按日期进行统计
        // 整个蓝牙设备表里有很多重复的设备，查找的时候需要进行去重
        bluetoothDeviceEntityDao = dbHelper.getSession().getBluetoothDeviceEntityDao();

        // 获取到数据库中的所有数据
        deviceEntityList = bluetoothDeviceEntityDao.loadAll();

        // 格式化年月日
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Map<String, List<BluetoothDeviceEntity>> dateMap = new HashMap<>();
        for(BluetoothDeviceEntity entity: deviceEntityList) {
            // 【年-月-日】形式的日期
            // 按照相同的日期对设备进行分组
            String date = formatter.format(entity.getDate());
            if(!dateMap.containsKey(date)) {
                List<BluetoothDeviceEntity> list = new ArrayList<>();
                list.add(entity);
                dateMap.put(date, list);
            } else {
                List<BluetoothDeviceEntity> list = dateMap.get(date);
                list.add(entity);
            }
        }
        List<BluetoothDeviceEntity> distinctList = new ArrayList<>();
        // 遍历dateMap
        // 对相同日期的设备，去除重复设备
        // 日期-接触到的设备数
        Map<String, Integer> personNums = new HashMap<>();

        for (Map.Entry<String, List<BluetoothDeviceEntity>> entry: dateMap.entrySet()) {
            // 存在的设备
            Map<String, Boolean> deviceExist = new HashMap<>();
            // 当前日期
            String date = entry.getKey();
            // 当前日期的所有设备
            List<BluetoothDeviceEntity> list = entry.getValue();
            for(BluetoothDeviceEntity entity: list) {
                if(!deviceExist.containsKey(entity.getMacAddress())) {
                    deviceExist.put(entity.getMacAddress(), true);
                }
            }

            personNums.put(date, deviceExist.size());
        }

        // 现在有 日期-设备数 这个Map
        // 已知Today，求前14天的设备数量
        Integer[] ans = new Integer[14];
        int index = 13;
        // String转为Date
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        // days14
        ArrayList arrayList = new ArrayList();

        SimpleDateFormat formatter1 = new SimpleDateFormat("MM-dd");

        // 拿出14天的
        while(index >= 0) {
            // 往前挪一天
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date before = calendar.getTime();
            // 结果数组
            String d = formatter.format(before);
            ans[index] = personNums.get(d);

            String dd = formatter1.format(before);
            arrayList.add(dd);

            index--;
        }
        Collections.reverse(arrayList);

        AAChartView aaChartView = findViewById(R.id.AAChartView_bluetooth);
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Area)
                .title("14天接触人数统计")
                .backgroundColor("#ffffff")//#4b2b7f
                .categories((String[]) arrayList.toArray(new String[0]))
                .dataLabelsEnabled(false)
                .yAxisGridLineWidth(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("人数")
                                .data(ans),
                });
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel);
    }


}
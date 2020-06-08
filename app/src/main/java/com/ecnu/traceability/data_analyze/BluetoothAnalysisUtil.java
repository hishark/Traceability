package com.ecnu.traceability.data_analyze;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntityDao;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BluetoothAnalysisUtil {
    private DBHelper dbHelper = null;
    BluetoothDeviceEntityDao bluetoothDeviceEntityDao;
    List<BluetoothDeviceEntity> deviceEntityList;
    public BluetoothAnalysisUtil(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Bundle processData() {
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
            ans[index] =  null==personNums.get(d)? 0:personNums.get(d);

            String dd = formatter1.format(before);
            arrayList.add(dd);

            index--;
        }
        Collections.reverse(arrayList);
        Bundle bundle = new Bundle();
        bundle.putSerializable("countMap", ans);
        bundle.putSerializable("dateList", arrayList);
        return bundle;

    }


}

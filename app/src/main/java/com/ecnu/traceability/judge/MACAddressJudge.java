package com.ecnu.traceability.judge;

import android.database.Cursor;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntityDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 判断当前设备和患者是否有过接触
 */
public class MACAddressJudge {
    private DBHelper dbHelper = null;
    private String MACFromServer = "test";
    private List<String> patientsMacList;
    private List<String> localMacList;
    private List<String> meetList;

    public MACAddressJudge(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     *  从服务器拿到患者的MAC ADDRESS - List
     */
    public void getMACAddressFromServer() {
//        String url="";//网址
//        HTTPUtils.getDataFromServer("", new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//              MACFromServer=
//            }


        /**
         *  先整点假数据
         */
        patientsMacList = new ArrayList<>();
        patientsMacList.add("54:33:CB:8A:22:E1"); // 我滴手机
        patientsMacList.add("B8:C9:B5:36:03:1C"); // 本地的
        patientsMacList.add("E0:1F:88:D9:C5:9E"); // 邻居手机
        patientsMacList.add("14:23:3B:8A:22:E2"); // 瞎编的
        patientsMacList.add("14:13:1B:1A:12:1E"); // 瞎编的

    }

    public List<BluetoothDeviceEntity> getDataFromDatabase() {
        localMacList = new ArrayList<>();
        String SQL_DISTINCT = "SELECT DISTINCT MAC_ADDRESS FROM "+BluetoothDeviceEntityDao.TABLENAME;
        Cursor cursor =  dbHelper.getSession().getDatabase().rawQuery(SQL_DISTINCT , null);
        // 取出三个字段分别对应的索引，下面再对着索引去取值
        int macAddressIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.MacAddress.columnName);
//        int deviceNameIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.DeviceName.columnName);
//        int dateIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.Date.columnName);

        if (macAddressIndex != -1) {// && deviceNameIndex != -1 && dateIndex != -1
            while (cursor.moveToNext()) {
                String macAddress = cursor.getString(macAddressIndex);
//                String deviceName = cursor.getString(deviceNameIndex);
//                Double date = cursor.getDouble(dateIndex);
                // 这里取到三个字段 自己是存模型还是字典 自己处理。

                localMacList.add(macAddress);

            }
        }

        // 本地的
        List<BluetoothDeviceEntity> list = null;
        if (null != MACFromServer) {
            list = dbHelper.getSession().getBluetoothDeviceEntityDao().queryBuilder()
                    .orderAsc(BluetoothDeviceEntityDao.Properties.Date)//通过 StudentNum 这个属性进行正序排序
                    .where(BluetoothDeviceEntityDao.Properties.MacAddress.eq(MACFromServer))//数据筛选，只获取 Name = "zone" 的数据。
                    .build()
                    .list();
            return list;
        } else {
            return null;
        }
    }

    /**
     * 得到接触过的
     */
    public int getMeetMacList() {
        meetList = new ArrayList<>();
        // 从服务器获取到患者的mac address列表 patientsMacList
        getMACAddressFromServer();
        // 从本地数据库里获取到接触过的Mac Address列表
        getDataFromDatabase();

//        Map<String,Integer> meetMap=new HashMap<String,Integer>();
        // 获取到两个列表之后进行比对
        // 得到接触过的Mac Address列表
        for (String macAddress: patientsMacList) {
            if (localMacList.contains(macAddress)) {
//                meetList.add(macAddress);
//                meetMap.put(macAddress,)
            }
        }

        return meetList.size();
    }
}

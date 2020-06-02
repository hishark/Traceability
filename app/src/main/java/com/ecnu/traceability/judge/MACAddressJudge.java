package com.ecnu.traceability.judge;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntityDao;

import java.util.List;

public class MACAddressJudge {
    private DBHelper dbHelper = null;
    private String MACFromServer = null;

    public MACAddressJudge(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

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
    }

    public List<BluetoothDeviceEntity> getDataFromDatabase() {
//        String SQL_DISTINCT = "SELECT DISTINCT "+ BluetoothDeviceEntityDao.Properties.MacAddress+" FROM "+BluetoothDeviceEntityDao.TABLENAME;
//        Cursor cursor =  dbHelper.getSession().getDatabase().rawQuery(SQL_DISTINCT , null);
//        // 取出三个字段分别对应的索引，下面再对着索引去取值
//        int macAddressIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.MacAddress.columnName);
//        int deviceNameIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.DeviceName.columnName);
//        int dataIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.Date.columnName);
//
//        if (macAddressIndex != -1 && deviceNameIndex != -1 && dataIndex != -1) {
//            while (cursor.moveToNext()) {
//                String macAddress = cursor.getString(macAddressIndex);
//                String deviceName = cursor.getString(deviceNameIndex);
//                Double data = cursor.getDouble(dataIndex);
//                // 这里取到三个字段 自己是存模型还是字典 自己处理。
//            }
//        }
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

    public int judge() {
        int count = 0;
        getMACAddressFromServer();
        List<BluetoothDeviceEntity> list = getDataFromDatabase();
        if (null != list) {
            count = list.size();
        }
        return count;
    }
}

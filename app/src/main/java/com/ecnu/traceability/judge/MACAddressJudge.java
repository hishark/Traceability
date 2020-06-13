package com.ecnu.traceability.judge;

import android.database.Cursor;
import android.util.Log;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.HTTPUtils;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntityDao;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 判断当前设备和患者是否有过接触
 */
public class MACAddressJudge {
    private static final int STRENGTH_THRESHOLD = -100;
    private DBHelper dbHelper = null;
    private String MACFromServer = "test";
    private List<String> patientsMacList;
    private List<String> localMacList;
    private List<String> meetList;

    public MACAddressJudge(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 从服务器拿到患者的MAC ADDRESS - List
     */
    public void getMACAddressFromServer() {

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

    /**
     * 查询数据库中病人mac地址的记录
     * @param patientMacAddress
     * @return
     */
    public List<BluetoothDeviceEntity> getDataFromDatabase(String patientMacAddress) {
        //        localMacList = new ArrayList<>();
        //        String SQL_DISTINCT = "SELECT DISTINCT MAC_ADDRESS FROM "+BluetoothDeviceEntityDao.TABLENAME;
        //        Cursor cursor =  dbHelper.getSession().getDatabase().rawQuery(SQL_DISTINCT , null);
        //        // 取出三个字段分别对应的索引，下面再对着索引去取值
        //        int macAddressIndex = cursor.getColumnIndex(BluetoothDeviceEntityDao.Properties.MacAddress.columnName);
        //
        //        if (macAddressIndex != -1) {// && deviceNameIndex != -1 && dateIndex != -1
        //            while (cursor.moveToNext()) {
        //                String macAddress = cursor.getString(macAddressIndex);
        //                // 这里取到三个字段 自己是存模型还是字典 自己处理。
        //                localMacList.add(macAddress);
        //            }
        //        }
        // 本地的
        List<BluetoothDeviceEntity> list = null;
        if (null != patientMacAddress) {
            list = dbHelper.getSession().getBluetoothDeviceEntityDao().queryBuilder()
                    .orderAsc(BluetoothDeviceEntityDao.Properties.Date)//通过 StudentNum 这个属性进行正序排序
                    .where(BluetoothDeviceEntityDao.Properties.MacAddress.eq(patientMacAddress))//数据筛选，只获取MacAddress。
                    .build()
                    .list();
            return list;
        } else {
            return null;
        }
    }

    /**
     * 统计接触记录中有多少条大于信号强度阈值
     *
     * @param localDataList 本地数据库中与病人的Mac地址接触的记录
     * @return 满足大于阈值的记录条数
     */
    public int judge(List<BluetoothDeviceEntity> localDataList) {
        int count = 0;
        for (BluetoothDeviceEntity entity : localDataList) {
            if (entity.getSignalStrength() > STRENGTH_THRESHOLD) {
                addRiskToDB(entity);//将风险记录插入数据库持久化
                count++;
            }
        }
        return count * 3;
    }


    public void addRiskToDB(BluetoothDeviceEntity macAddressEntity) {
        dbHelper.getSession().getMacRiskDao().insert(new MacRisk(macAddressEntity.getMacAddress(), macAddressEntity.getDate()));
    }

}

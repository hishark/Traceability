package com.ecnu.traceability.bluetooth.service;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ecnu.traceability.bluetooth.Dao.BluetoothDeviceEntity;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;

public class IBluetoothService extends IntentService {
    private static final String TAG = "IBluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler();
    private DBHelper dbHelper = DBHelper.getInstance();


    public IBluetoothService() {
        super("IBluetoothService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        Log.e(TAG,"===========================================");
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
        dbHelper.init(this);
        registerDiscoveryReceiver();

        while (true) {
            startScan();
//            Log.e(TAG,"===========================================");
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.e(TAG,"=======================收到====================");
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        Log.i(TAG, "正在搜索附近的蓝牙设备");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        Log.i(TAG, "搜索结束");
//                        Log.i(TAG,"===========================================");
                        break;
                    case BluetoothDevice.ACTION_FOUND:
//                        Log.e(TAG,"found---------------found-------------found");
                        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        BluetoothDeviceEntity deviceInfo = new BluetoothDeviceEntity(rssi, bluetoothDevice);
                        dbHelper.getSession().getBluetoothDeviceEntityDao().insert(deviceInfo);
                        break;
                }
            }
        }
    };


    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Log.e(TAG, "------------------------onDestroy---------------------");
        unregisterReceiver(discoveryReceiver);
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }


    private void registerDiscoveryReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(discoveryReceiver, intentFilter);
    }

    private void startScan() {
        if (bluetoothAdapter.isEnabled()) {
            scanDevice();
//            Log.e(TAG,"=====================start scan======================");
        } else {
            if (bluetoothAdapter.enable()) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanDevice();
                    }
                }, 1500);
            } else {
                GeneralUtils.showToastInService(getApplicationContext(), "请求蓝牙权限被拒绝，请授权");
                Log.e(String.valueOf(this.getClass()), "请求蓝牙权限被拒绝，请授权");
            }
        }
    }

    private void scanDevice() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

}

package com.ecnu.traceability;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.NotificationsPermissionUtil;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.bluetooth.service.MacAddress;
import com.ecnu.traceability.model.User;

import static com.amap.api.maps.model.BitmapDescriptorFactory.getContext;

public class SignupActivity extends BaseActivity {
    private static final int REQUEST_PERMISSION = 10;

    private DBHelper dbHelper = DBHelper.getInstance();

    private EditText macAddressEdit = null;
    private EditText phoneEdit = null;
    private EditText addressEdit = null;
    private Button confirmBtn = null;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        verifyStoragePermission(SignupActivity.this);
        if(!NotificationsPermissionUtil.isNotificationEnabled(this)) {
            NotificationsPermissionUtil.openPush(this);
        }
        macAddressEdit = findViewById(R.id.user_bluetooth_mac_address);
        phoneEdit = findViewById(R.id.user_phone);
        addressEdit = findViewById(R.id.user_address);
        confirmBtn = findViewById(R.id.submit_button);
        String mac = MacAddress.getBtAddressByReflection();
        dbHelper.init(this);
        if (null != mac) {
            macAddressEdit.setText(mac);
        } else {
            Toast.makeText(this, "无法准确获取本机蓝牙地址，请按照提示去设置中查找", 5000).show();
        }
        confirmBtn.setOnClickListener(listener);

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String mac = String.valueOf(macAddressEdit.getText());
            String tel = String.valueOf(phoneEdit.getText());
            String address = String.valueOf(addressEdit.getText());
            if (mac.equals("") || mac.trim().length() != 17 || mac.split(":").length != 6) {
                macAddressEdit.setHint("蓝牙地址不正确");
                macAddressEdit.setHintTextColor(Color.RED);
                return;
            }
            if (tel.equals("") || tel.trim().length() != 11) {
                phoneEdit.setHint("手机号码不正确");
                phoneEdit.setHintTextColor(Color.RED);
                return;
            }
            if (address.equals("")) {
                addressEdit.setHint("住址不能为空");
                addressEdit.setHintTextColor(Color.RED);
                return;
            }
            User user = new User(mac, tel, address);
            dbHelper.getSession().getUserDao().insert(user);

            OneNetDeviceUtils.getDevices(getContext(), dbHelper);
            startActivity(HomepageActivity.class);

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        checkSelfPermission();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 在这里，拦截或者监听Android系统的返回键事件。
            // return将拦截。
            // 不做任何处理则默认交由Android系统处理。
        }

        return false;
    }

    // read and write permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //获取存储权限
    public static void verifyStoragePermission(Activity activity) {
        // Get permission status
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission we request it
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    private void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SignupActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        showToast("请授予权限");
                        finish();
                        return;
                    }
                }
                break;
        }
    }
}
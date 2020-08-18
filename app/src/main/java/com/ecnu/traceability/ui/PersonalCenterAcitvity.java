package com.ecnu.traceability.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ecnu.traceability.BaseActivity;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.model.User;

import java.util.List;

public class PersonalCenterAcitvity extends BaseActivity {
    private DBHelper dbHelper = DBHelper.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_center_activity);
        setTitle("个人中心");

        dbHelper.init(this);
        TextView macTv = findViewById(R.id.mac_address_text);
        TextView phoneTv = findViewById(R.id.phone_text);
        TextView addressTv = findViewById(R.id.address_text);
        TextView devIdTv = findViewById(R.id.device_id_text);
        TextView is_segregate_Tv = findViewById(R.id.is_segregate_text);
        TextView segregate_time_Tv = findViewById(R.id.left_segregate_time_text);

        String macAddress = OneNetDeviceUtils.macAddress;

        if (null != macAddress) {
            macTv.setText(macAddress);
        } else {
            macTv.setText("出错了");
        }
        String tel = "出错了";
        String address="出错了";
        List<User> users = dbHelper.getSession().getUserDao().loadAll();
        if (null != users && users.size() > 0) {
            User user = users.get(0);
            tel = user.getTel();
            address=user.getAddress();
        }
        phoneTv.setText(tel);
        addressTv.setText(address);


        String deviceId="出错了";
        List<LocalDevice> deviceList = dbHelper.getSession().getLocalDeviceDao().loadAll();
        if(null!=deviceList&&deviceList.size()>0){
            deviceId= deviceList.get(0).getDeviceId();

        }

        devIdTv.setText(deviceId);


    }
}

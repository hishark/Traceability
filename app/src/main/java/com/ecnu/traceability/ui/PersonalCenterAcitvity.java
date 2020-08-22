package com.ecnu.traceability.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ecnu.traceability.BaseActivity;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.Utils.OneNetDeviceUtils;
import com.ecnu.traceability.model.LocalDevice;
import com.ecnu.traceability.model.User;

import java.util.List;

public class PersonalCenterAcitvity extends BaseActivity {
    private DBHelper dbHelper = DBHelper.getInstance();

    private Button btEditInfo;

    private boolean isEdit = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_center_activity);
        setTitle("个人中心");

        dbHelper.init(this);
        EditText etMac = findViewById(R.id.personal_mac_address);
        EditText etPhone = findViewById(R.id.personal_phone);
        EditText etAddress = findViewById(R.id.personal_address);

        TextView devIdTv = findViewById(R.id.device_id_text);
        TextView is_segregate_Tv = findViewById(R.id.is_segregate_text);
        TextView segregate_time_Tv = findViewById(R.id.left_segregate_time_text);

        btEditInfo = findViewById(R.id.personal_edit);

        String macAddress = OneNetDeviceUtils.macAddress;

        if (null != macAddress) {
            etMac.setText(macAddress);
        } else {
            etMac.setText("出错了");
        }
        String tel = "出错了";
        String address="出错了";
        List<User> users = dbHelper.getSession().getUserDao().loadAll();
        if (null != users && users.size() > 0) {
            User user = users.get(0);
            tel = user.getTel();
            address=user.getAddress();
        }
        etPhone.setText(tel);
        etAddress.setText(address);


        String deviceId="出错了";
        List<LocalDevice> deviceList = dbHelper.getSession().getLocalDeviceDao().loadAll();
        if(null!=deviceList&&deviceList.size()>0){
            deviceId= deviceList.get(0).getDeviceId();
        }

        devIdTv.setText(deviceId);

        etMac.setEnabled(false);
        // 初始状态下手机号和住址都无法更改
        etPhone.setEnabled(false);
        etAddress.setEnabled(false);


        btEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEdit) {
                    btEditInfo.setText("确认修改");
                    isEdit = true;
                    etPhone.setEnabled(true);
                    etAddress.setEnabled(true);


                } else {


                    AlertDialog.Builder builder = new AlertDialog.Builder(PersonalCenterAcitvity.this);
                    AlertDialog dialog = builder.setTitle("提醒")
                            .setMessage("您确定要修改个人资料吗？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String tel = etPhone.getText().toString().trim();
                                    String address = etAddress.getText().toString().trim();
                                    if (tel.equals("") || address.equals("")) {
                                        GeneralUtils.showToastInService(getApplicationContext(), "您还未填写信息");
                                        return;
                                    }

                                    btEditInfo.setText("修改资料");
                                    isEdit = false;
                                    etPhone.setEnabled(false);
                                    etAddress.setEnabled(false);

                                    updateUserInfo(tel, address);
                                }
                            })
                            .create();
                    dialog.show();
                }
            }
        });

    }

    // 更新用户信息
    private void updateUserInfo(String tel, String address) {
        List<User> users = dbHelper.getSession().getUserDao().loadAll();
        User curUser;
        if (null != users && users.size() > 0) {
            curUser = users.get(0);
            curUser.setTel(tel);
            curUser.setAddress(address);
            dbHelper.getSession().getUserDao().update(curUser);
            GeneralUtils.showToastInService(getApplicationContext(), "修改成功！");
        } else {
            GeneralUtils.showToastInService(getApplicationContext(), "出错啦！用户不存在！");
        }
    }
}

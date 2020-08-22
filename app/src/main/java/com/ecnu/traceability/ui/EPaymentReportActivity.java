package com.ecnu.traceability.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.amap.api.location.DPoint;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ecnu.traceability.BaseActivity;
import com.ecnu.traceability.R;
import com.ecnu.traceability.Utils.DBHelper;
import com.ecnu.traceability.Utils.GeneralUtils;
import com.ecnu.traceability.ePayment.Dao.EPaymentEntity;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EPaymentReportActivity  extends BaseActivity implements GeocodeSearch.OnGeocodeSearchListener{

    private String date_ = null;
    private String time_ = null;

    private EditText etPayLoc;
    private Button btTimeSelect;
    private Button btSubmit;

    private DBHelper dbHelper;
    private Date dateProcessed;
    private static final String TAG = "EPaymentReportActivity";

    public GeocodeSearch geocoderSearch = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_payment_report);
        setTitle("");

        initView();
        dbHelper = DBHelper.getInstance();
        dbHelper.init(this);

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);

        Calendar now = Calendar.getInstance();
        android.app.DatePickerDialog dateDialog = new android.app.DatePickerDialog(
                this,
                (view1, year, month, dayOfMonth) -> {
                    date_ = year + "-" + month + "-" + dayOfMonth;
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        android.app.TimePickerDialog timeDialog = new android.app.TimePickerDialog(
                this,
                (view11, hour, minute) -> {
                    time_ = hour + ":" + minute + ":" + 0;
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );

        btTimeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog.show();
                dateDialog.show();
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payLocation = etPayLoc.getText().toString().trim();
                if (payLocation.equals("")) {
                    GeneralUtils.showToastInService(getApplicationContext(), "以上信息不能为空");
                    return;
                }

                if (date_ == null) {
                    GeneralUtils.showToastInService(getApplicationContext(), "请选择日期");
                    return;
                }
                if (time_ == null) {
                    GeneralUtils.showToastInService(getApplicationContext(), "请选择时间");
                    return;
                }

                String date=date_+" "+time_;
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    dateProcessed=sdf.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    dateProcessed=null;
                }
                EPaymentEntity entity = new EPaymentEntity();
                dbHelper.getSession().getEPaymentEntityDao().insert(entity);

                locationToLatlon(etPayLoc.getText().toString().trim());

            }
        });

    }

    private void initView() {
        etPayLoc = findViewById(R.id.et_pay_location);
        btTimeSelect = findViewById(R.id.pay_bt_timeselect);
        btSubmit = findViewById(R.id.pay_bt_submit);
    }


    /**
     * 逆地理坐标转换
     * @param address
     */
    public void locationToLatlon(String address) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
//        RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
        GeocodeQuery query=new GeocodeQuery(address,"");
        geocoderSearch.getFromLocationNameAsyn(query);//异步方法
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.e("LocationAnalysis", regeocodeResult.getRegeocodeAddress().getCity());
        regeocodeResult.getRegeocodeAddress().getCity();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        try {
            LatLonPoint latlon = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
            Log.i(TAG, latlon.getLatitude()+"\t"+latlon.getLongitude());
            Double lat = latlon.getLatitude();
            Double lon = latlon.getLongitude();
            EPaymentEntity entity = new EPaymentEntity(lat, lon, dateProcessed);
            dbHelper.getSession().getEPaymentEntityDao().insert(entity);
        } catch (Exception e) {
            GeneralUtils.showToastInService(getApplicationContext(), "出错啦！提交失败！");
        }
        GeneralUtils.showToastInService(getApplicationContext(),"提交成功");
        etPayLoc.setText("");
        date_=null;
        time_=null;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

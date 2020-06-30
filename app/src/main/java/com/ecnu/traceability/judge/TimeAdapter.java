package com.ecnu.traceability.judge;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ecnu.traceability.R;
import com.ecnu.traceability.location.Dao.LocationEntity;
import com.ecnu.traceability.ui.ShowPop;
import com.lxj.xpopup.XPopup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TimeAdapter extends BaseAdapter implements GeocodeSearch.OnGeocodeSearchListener {

    private Context context;
    private List<LocationEntity> meetTimeList;
    private LayoutInflater inflater;
    public GeocodeSearch geocoderSearch = null;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int curPos = 0;


    public TimeAdapter(Context c, List<LocationEntity> list) {
        this.context = c;
        this.meetTimeList = list;
        this.inflater = LayoutInflater.from(c);

        geocoderSearch = new GeocodeSearch(c);
        geocoderSearch.setOnGeocodeSearchListener(this);

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.e("LocationAnalysis", regeocodeResult.getRegeocodeAddress().getFormatAddress());
        ShowPop pop = new ShowPop(context);

        Date date = meetTimeList.get(curPos).getDate();

        pop.setText("接触时间：" + sdf.format(date),
                "接触地点: " + regeocodeResult.getRegeocodeAddress().getFormatAddress());
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .isCenterHorizontal(true)
                .offsetY(200)
                .asCustom(pop)
                .show();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    public void latlonToLocation(LatLonPoint point) throws AMapException {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);//异步方法
    }

    @Override
    public int getCount() {
        return meetTimeList.size();
    }

    @Override
    public Object getItem(int position) {
        return meetTimeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = inflater.inflate(R.layout.meet_time_item, null);
            viewHolder = new TimeAdapter.ViewHolder();
            viewHolder.tvTime = view.findViewById(R.id.tv_meet_time);
            viewHolder.tvCity = view.findViewById(R.id.tv_meet_city);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (TimeAdapter.ViewHolder) view.getTag();
        }

        if (meetTimeList.get(position) != null) {
            String time = sdf.format(meetTimeList.get(position).getDate());
            viewHolder.tvTime.setText(time);
            viewHolder.tvCity.setText("纬度：" + meetTimeList.get(position).getLatitude() + "\n经度: " + meetTimeList.get(position).getLongitude());

            viewHolder.tvCity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //Log.i("on click", "位置是"+String.valueOf(position));
                        curPos = position;
                        latlonToLocation(new LatLonPoint(meetTimeList.get(position).getLatitude(), meetTimeList.get(position).getLongitude()));
                    } catch (AMapException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return view;
    }

    class ViewHolder {
        TextView tvTime;
        TextView tvCity;
    }
}

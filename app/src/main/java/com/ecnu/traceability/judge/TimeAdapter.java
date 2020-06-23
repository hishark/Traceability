package com.ecnu.traceability.judge;

import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.List;

public class TimeAdapter extends BaseAdapter {

    private Context context;
    private List<LocationEntity> meetTimeList;
    private LayoutInflater inflater;
    public GeocodeSearch geocoderSearch = null;


    public TimeAdapter(Context c, List<LocationEntity> list) {
        this.context = c;
        this.meetTimeList = list;
        this.inflater = LayoutInflater.from(c);

        geocoderSearch = new GeocodeSearch(c);
//        geocoderSearch.setOnGeocodeSearchListener(this);

    }

    public String  latlonToLocation(LatLonPoint point) throws AMapException {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
        RegeocodeAddress result=geocoderSearch.getFromLocation(query);//异步方法
        return result.getCity();
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
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (TimeAdapter.ViewHolder) view.getTag();
        }

        if (meetTimeList.get(position) != null) {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time=sdf.format(meetTimeList.get(position).getDate());
            String city="";
            try {
                city=latlonToLocation(new LatLonPoint( meetTimeList.get(position).getLatitude(),meetTimeList.get(position).getLongitude()));
            } catch (AMapException e) {
                e.printStackTrace();
            }
            viewHolder.tvTime.setText(city+time);
        }

        return view;
    }

    class ViewHolder {
        TextView tvTime;
    }
}

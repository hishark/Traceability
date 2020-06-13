package com.ecnu.traceability.judge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecnu.traceability.R;
import com.ecnu.traceability.location.Dao.LocationEntity;

import java.text.SimpleDateFormat;
import java.util.List;

public class TimeAdapter extends BaseAdapter {

    private Context context;
    private List<LocationEntity> meetTimeList;
    private LayoutInflater inflater;

    public TimeAdapter(Context c, List<LocationEntity> list) {
        this.context = c;
        this.meetTimeList = list;
        this.inflater = LayoutInflater.from(c);
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
            viewHolder.tvTime.setText(time);
        }

        return view;
    }

    class ViewHolder {
        TextView tvTime;
    }
}

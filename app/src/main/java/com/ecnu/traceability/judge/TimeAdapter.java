package com.ecnu.traceability.judge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecnu.traceability.R;

import java.util.List;

public class TimeAdapter extends BaseAdapter {

    private Context context;
    private List<String> meetTimeList;
    private LayoutInflater inflater;

    public TimeAdapter(Context c, List<String> list) {
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
            viewHolder.tvTime.setText(meetTimeList.get(position));
        }

        return view;
    }

    class ViewHolder {
        TextView tvTime;
    }
}

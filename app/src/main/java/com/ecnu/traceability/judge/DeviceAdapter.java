package com.ecnu.traceability.judge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.iot.onenet.module.Device;
import com.ecnu.traceability.R;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<String> macAddressList;
    private LayoutInflater inflater;

    public DeviceAdapter(Context context, List<String> macList) {
        this.context = context;
        this.macAddressList = macList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return macAddressList.size();
    }

    @Override
    public Object getItem(int position) {
        return macAddressList.get(position);
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
            view = inflater.inflate(R.layout.device_item, null);
            viewHolder = new DeviceAdapter.ViewHolder();
            viewHolder.imgDevice = view.findViewById(R.id.img_device);
            viewHolder.tvDevice = view.findViewById(R.id.tv_device_mac_address);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (DeviceAdapter.ViewHolder) view.getTag();
        }

        if (macAddressList.get(position) != null) {
            viewHolder.tvDevice.setText(macAddressList.get(position));
        }

        return view;
    }

    class ViewHolder {
        ImageView imgDevice;
        TextView tvDevice;
    }
}

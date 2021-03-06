package com.ecnu.traceability.judge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecnu.traceability.R;
import com.ecnu.traceability.transportation.Dao.TransportationEntity;

import java.text.SimpleDateFormat;
import java.util.List;

public class TransAdapter extends BaseAdapter  implements View.OnClickListener {
    private Context context;
    private List<TransportationEntity> transRiskList;
    private LayoutInflater inflater;
    private InnerItemOnclickListener mListener;

    public TransAdapter(Context context, List<TransportationEntity> transRiskList) {
        this.context = context;
        this.transRiskList = transRiskList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return transRiskList.size();
    }

    @Override
    public Object getItem(int index) {
        return transRiskList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup viewGroup) {
        View view;
        TransAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            view = inflater.inflate(R.layout.trans_item, null);
            viewHolder = new TransAdapter.ViewHolder();
            viewHolder.imgDevice = view.findViewById(R.id.img_trans);
            viewHolder.tvTransType = view.findViewById(R.id.tv_trans_info_type);
            viewHolder.tvTransNo = view.findViewById(R.id.tv_trans_info_no);
            viewHolder.tvTransSeat = view.findViewById(R.id.tv_trans_info_seat);
            viewHolder.tvTransTime= view.findViewById(R.id.tv_trans_info_time);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (TransAdapter.ViewHolder) view.getTag();
        }

        if (transRiskList.get(index) != null) {
            viewHolder.tvTransType.setText(transRiskList.get(index).getType());
            viewHolder.tvTransNo.setText(transRiskList.get(index).getNO());
            viewHolder.tvTransSeat.setText(transRiskList.get(index).getSeat().toString());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            viewHolder.tvTransTime.setText(formatter.format(transRiskList.get(index).getDate()));
        }

        return view;
    }

    class ViewHolder {
        ImageView imgDevice;
        TextView tvTransType;
        TextView tvTransNo;
        TextView tvTransSeat;
        TextView tvTransTime;
    }

    @Override
    public void onClick(View view) {
        mListener.itemClick(view);
    }
    interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener) {
        this.mListener = listener;
    }
}

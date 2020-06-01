package com.ecnu.traceability.Utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.amap.api.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class LocationKmeans extends Kmeans<LatLng> {
    public static final String TAG = "LocationKmeans";
    private LatLng center = null;

    public LatLng getCenter(List<LatLng> pointList) {

        double x = 0;
        double y = 0;
        for (LatLng point : pointList) {
            x += point.latitude;
            y += point.longitude;
        }
        center = new LatLng(x / pointList.size(), y / pointList.size());
        return center;

    }

    Comparator<LatLng> comparator = new Comparator<LatLng>() {
        @Override
        public int compare(LatLng o1, LatLng o2) {
            double disO1 = (o1.latitude - center.latitude) * (o1.latitude - center.latitude) + (o1.longitude - center.longitude) * (o1.longitude - center.longitude);
            double disO2 = (o2.latitude - center.latitude) * (o2.latitude - center.latitude) + (o2.longitude - center.longitude) * (o2.longitude - center.longitude);
            if (disO1 - disO2 > 0)
                return -1;
            else if (disO1 - disO2 < 0)
                return 1;
            else return 0;
        }
    };

    @Override
    public List<List<LatLng>> clustering() {
        List<LatLng> dataArray=super.getDataArray();
        getCenter(dataArray);
        int K=super.getK();
        if (dataArray == null) {
            return null;
        }
        //初始K个点为数组中的前K个点
        int size = K >dataArray.size() ? dataArray.size() : K;
        Queue<LatLng> queue = new PriorityQueue<LatLng>(dataArray.size(), comparator);
        for (int i = 0; i < dataArray.size(); i++) {
            queue.add(dataArray.get(i));
        }
        List<LatLng> centerT = new ArrayList<LatLng>(size);

        for (int i = 0; i < size; i++) {
            centerT.add(queue.poll());
        }
        //对数据进行打乱
        //        Collections.shuffle(dataArray);
        //        for (int i = 0; i < size; i++) {
        //            centerT.add(dataArray.get(i));
        //        }

        clustering(centerT, 0);
        return super.getClusterList();
    }

    @Override
    public double similarScore(LatLng o1, LatLng o2) {
        //        φ1, φ2 are the latitude of point 1 and latitude of point 2 (in radians),
        //        λ1, λ2 are the longitude of point 1 and longitude of point 2 (in radians).
//
//        double r = 6371393;
//        double item1 = Math.pow(Math.sin((o1.latitude + o2.latitude) * 1.0 / 2), 2);
//        double item2 = Math.cos(o1.latitude);
//        double item3 = Math.cos(o2.latitude);
//        double item4 = Math.pow(Math.sin((o1.longitude + o2.longitude) * 1.0 / 2), 2);
//
//        double inner = item1 + item2 * item3 * item4;
//
//        double distance = 2 * r * Math.asin(Math.sqrt(inner));
//
        double distance = Math.sqrt((o1.latitude - o2.latitude) * (o1.latitude - o2.latitude) + (o1.longitude - o2.longitude) * (o1.longitude - o2.longitude));
//        Log.e(TAG, String.valueOf(distance));

        return distance * -1;
    }

    @Override
    public boolean equals(LatLng o1, LatLng o2) {
        return o1.latitude == o2.latitude && o1.longitude == o2.longitude;
    }

    @Override
    public LatLng getCenterT(List<LatLng> list) {
        double x = 0;
        double y = 0;
        DecimalFormat df = new DecimalFormat("#.######");// 保留小数点后6位
        try {
            for (LatLng LatLng : list) {
                x += LatLng.latitude;
                y += LatLng.longitude;
            }

            x = Double.parseDouble(df.format(x / list.size()));
            y = Double.parseDouble(df.format(y / list.size()));
//            x = x / list.size();
//            y = y / list.size();
        } catch (Exception e) {
            System.out.println("divide zero");
            Log.e(TAG, "=================divide zero===================");
        }
        return new LatLng(x, y);
    }
}

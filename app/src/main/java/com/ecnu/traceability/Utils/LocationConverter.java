package com.ecnu.traceability.Utils;

import com.ecnu.traceability.location.Dao.LocationEntity;

public class LocationConverter  {


    public static double pi = 3.1415926535897932384626;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     */
    public static LocationEntity gcj02_To_Bd09(LocationEntity point) {
        double x = point.getLongitude(), y = point.getLatitude();
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        point.setLongitude(bd_lon);
        point.setLatitude(bd_lat);

        return point;
    }




}

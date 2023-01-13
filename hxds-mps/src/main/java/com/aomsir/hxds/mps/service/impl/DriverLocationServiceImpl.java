package com.aomsir.hxds.mps.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.mps.service.DriverLocationService;
import com.aomsir.hxds.mps.util.CoordinateTransform;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DriverLocationServiceImpl implements DriverLocationService {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void updateLocationCache(Map param) {
        long driverId = MapUtil.getLong(param, "driverId");
        String latitude = MapUtil.getStr(param, "latitude");
        String longitude = MapUtil.getStr(param, "longitude");


        int rangeDistance = MapUtil.getInt(param, "rangeDistance");    //接单范围
        int orderDistance = MapUtil.getInt(param, "orderDistance");    //订单里程范围

        //封装成Point对象才能缓存到Redis里面
        Point point = new Point(Convert.toDouble(longitude), Convert.toDouble(latitude));
        /*
         * 把司机实时定位缓存到Redis里面，便于Geo定位计算
         * Geo是集合形式，如果设置过期时间，所有司机的定位缓存就全都失效了,能修改但不能是设置过期时间
         * 正确做法是司机上线后，更新GEO中的缓存定位
         */

        // 将司机实时定位存放在RedisGeo中
        this.redisTemplate.opsForGeo()
                .add("driver_location", point, driverId + "");

        //定向接单地址的经度
        String orientateLongitude = null;
        if (param.get("orientateLongitude") != null) {
            orientateLongitude = MapUtil.getStr(param, "orientateLongitude");
        }
        //定向接单地址的纬度
        String orientateLatitude = null;
        if (param.get("orientateLatitude") != null) {
            orientateLatitude = MapUtil.getStr(param, "orientateLatitude");
        }

        //定向接单经纬度的字符串
        String orientation = "none";
        if (orientateLongitude != null && orientateLatitude != null) {
            orientation = orientateLatitude + "," + orientateLongitude;
        }

        /*
        * 为了解决判断哪些司机在线，我们还要单独弄一个上线缓存
        * 缓存司机的接单设置（定向接单、接单范围、订单总里程），便于系统判断该司机是否符合接单条件
         */
        String temp = rangeDistance + "#" + orderDistance + "#" + orientation;
        this.redisTemplate.opsForValue().set("driver_online#" + driverId, temp, 60, TimeUnit.SECONDS);
    }

    @Override
    public void removeLocationCache(long driverId) {
        //删除司机定位缓存
        this.redisTemplate.opsForGeo().remove("driver_location", driverId + "");
        //删除司机上线缓存
        this.redisTemplate.delete("driver_online#" + driverId);
    }


    @Override
    public ArrayList searchBefittingDriverAboutOrder(double startPlaceLatitude,
                                                     double startPlaceLongitude,
                                                     double endPlaceLatitude,
                                                     double endPlaceLongitude,
                                                     double mileage) {
        // 搜索订单起始点方圆5公里以内的司机
        Point point = new Point(startPlaceLongitude, startPlaceLatitude);
        Metric metric = RedisGeoCommands.DistanceUnit.KILOMETERS;   //设置GEO距离单位为千米
        Distance distance = new Distance(5, metric);
        Circle circle = new Circle(point, distance);

        //创建GEO参数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands
                .GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance() //结果中包含距离
                .includeCoordinates() //结果中包含坐标
                .sortAscending(); //升序排列

        //执行GEO计算，获得代驾地点5公里以内的司机
        GeoResults<RedisGeoCommands.GeoLocation<String>> radius = this.redisTemplate.opsForGeo()
                                                                    .radius("driver_location", circle, args);

        ArrayList list = new ArrayList(); //需要通知的司机列表

        // 不为空则附近有代驾订单
        if (radius != null) {
            Iterator<GeoResult<RedisGeoCommands.GeoLocation<String>>> iterator = radius.iterator();
            while (iterator.hasNext()) {
                GeoResult<RedisGeoCommands.GeoLocation<String>> result = iterator.next();
                RedisGeoCommands.GeoLocation<String> content = result.getContent();
                String driverId = content.getName();   // 获取5公里内的司机ID
                // Point memberPoint = content.getPoint(); // 对应的经纬度坐标
                double dist = result.getDistance().getValue(); // 距离中心点的距离

                // 排查掉不在线的司机
                if (!this.redisTemplate.hasKey("driver_online#" + driverId)) {
                    continue;
                }

                // 查找该司机的在线缓存
                Object obj = this.redisTemplate.opsForValue().get("driver_online#" + driverId);
                //如果查找的那一刻，缓存超时被置空，那么就忽略该司机
                if (obj == null) {
                    continue;
                }

                // 获取这个在线司机的信息
                String value = obj.toString();
                String[] temp = value.split("#");
                int rangeDistance = Integer.parseInt(temp[0]);
                int orderDistance = Integer.parseInt(temp[1]);
                String orientation = temp[2];

                //判断是否符合接单范围
                boolean bool_1 = dist <= rangeDistance;

                //判断订单里程是否符合
                boolean bool_2 = false;
                if (orderDistance == 0) {
                    bool_2 = true;
                } else if (orderDistance == 5 && mileage > 0 && mileage <= 5) {
                    bool_2 = true;
                } else if (orderDistance == 10 && mileage > 5 && mileage <= 10) {
                    bool_2 = true;
                } else if (orderDistance == 15 && mileage > 10 && mileage <= 15) {
                    bool_2 = true;
                } else if (orderDistance == 30 && mileage > 15 && mileage <= 30) {
                    bool_2 = true;
                }

                //判断定向接单是否符合
                boolean bool_3 = false;
                if (!orientation.equals("none")) {
                    double orientationLatitude = Double.parseDouble(orientation.split(",")[0]);
                    double orientationLongitude = Double.parseDouble(orientation.split(",")[1]);
                    //把定向点的火星坐标转换成GPS坐标
                    double[] location = CoordinateTransform.transformGCJ02ToWGS84(orientationLongitude, orientationLatitude);
                    GlobalCoordinates point_1 = new GlobalCoordinates(location[1], location[0]);
                    //把订单终点的火星坐标转换成GPS坐标
                    location = CoordinateTransform.transformGCJ02ToWGS84(endPlaceLongitude, endPlaceLatitude);
                    GlobalCoordinates point_2 = new GlobalCoordinates(location[1], location[0]);
                    //这里不需要Redis的GEO计算，直接用封装函数计算两个GPS坐标之间的距离
                    GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.WGS84, point_1, point_2);

                    //如果定向点距离订单终点距离在3公里以内，说明这个订单和司机定向点是顺路的
                    if (geoCurve.getEllipsoidalDistance() <= 3000) {
                        bool_3 = true;
                    }

                } else {
                    bool_3 = true;
                }

                //匹配接单条件
                if (bool_1 && bool_2 && bool_3) {
                    HashMap map = new HashMap() {{
                        put("driverId", driverId);
                        put("distance", dist);
                    }};
                    list.add(map); //把该司机添加到需要通知的列表中
                }
            }
        }
        return list;    // 返回最后可以接单的司机列表
    }

    @Override
    public void updateOrderLocationCache(Map param) {
        long orderId = MapUtil.getLong(param, "orderId");
        String latitude = MapUtil.getStr(param, "latitude");
        String longitude = MapUtil.getStr(param, "longitude");
        String location = latitude + "#" + longitude;
        this.redisTemplate.opsForValue().set("order_location#" + orderId, location, 10, TimeUnit.MINUTES);
    }

    @Override
    public HashMap searchOrderLocationCache(long orderId) {
        Object obj = this.redisTemplate.opsForValue().get("order_location#" + orderId);
        if (obj != null) {
            String[] temp = obj.toString().split("#");
            String latitude = temp[0];
            String longitude = temp[1];
            HashMap map = new HashMap() {{
                put("latitude", latitude);
                put("longitude", longitude);
            }};
            return map;
        }
        return null;
    }
}

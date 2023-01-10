package com.aomsir.hxds.mps.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.mps.service.MapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

@Service
@Slf4j
public class MapServiceImpl implements MapService {

    private String distanceUrl = "https://apis.map.qq.com/ws/distance/v1/matrix/";    //腾讯地图服务预估里程的API地址

    private String directionUrl = "https://apis.map.qq.com/ws/direction/v1/driving/";  //规划行进路线的API地址

    @Value("${tencent.map.key}")
    private String key;    // 腾讯地图服务

    public HashMap estimateOrderMileageAndMinute(String mode,
                                                 String startPlaceLatitude,
                                                 String startPlaceLongitude,
                                                 String endPlaceLatitude,
                                                  String endPlaceLongitude) {
        HttpRequest req = new HttpRequest(this.distanceUrl);   // 这个HttpRequest是hutool提供的
        req.form("mode", mode);                                                   // 步行/驾车/自行车
        req.form("from", startPlaceLatitude + "," + startPlaceLongitude);   // 起点的纬度,精度
        req.form("to", endPlaceLatitude + "," + endPlaceLongitude);         // 终点的纬度,精度
        req.form("key", key);                                                     // key

        // 发送请求,封装响应对象
        HttpResponse resp = req.execute();
        JSONObject json = JSONUtil.parseObj(resp.body());

        int status = json.getInt("status");      // 状态
        String message = json.getStr("message"); // 消息
        System.out.println(message);
        if (status != 0) {
            log.error(message);
            throw new HxdsException("预估里程异常：" + message);
        }
        JSONArray rows = json.getJSONObject("result").getJSONArray("rows");    // 获取返回的result
        JSONObject element = rows.get(0, JSONObject.class).getJSONArray("elements").get(0, JSONObject.class);
        int distance = element.getInt("distance");     // 提取距离
        String mileage = new BigDecimal(distance).divide(new BigDecimal(1000)).toString();
        int duration = element.getInt("duration");     // 提取时长
        String temp = new BigDecimal(duration).divide(new BigDecimal(60), 0, RoundingMode.CEILING).toString();
        int minute = Integer.parseInt(temp);

        HashMap map = new HashMap() {{
            put("mileage", mileage);
            put("minute", minute);
        }};
        return map;
    }


    @Override
    public HashMap calculateDriveLine(String startPlaceLatitude,
                                      String startPlaceLongitude,
                                      String endPlaceLatitude,
                                      String endPlaceLongitude) {
        HttpRequest req = new HttpRequest(this.directionUrl);
        req.form("from", startPlaceLatitude + "," + startPlaceLongitude);
        req.form("to", endPlaceLatitude + "," + endPlaceLongitude);
        req.form("key", key);

        HttpResponse resp = req.execute();
        JSONObject json = JSONUtil.parseObj(resp.body());
        int status = json.getInt("status");
        if (status != 0) {
            throw new HxdsException("执行异常");
        }
        JSONObject result = json.getJSONObject("result");   // 获取所有的线路,交给bff去获取最佳路线
        HashMap map = result.toBean(HashMap.class);
        return map;
    }
}

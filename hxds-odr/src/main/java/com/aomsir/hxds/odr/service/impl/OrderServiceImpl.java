package com.aomsir.hxds.odr.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.odr.db.dao.OrderDao;
import com.aomsir.hxds.odr.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;
    
    @Override
    public HashMap searchDriverTodayBusinessData(long driverId) {
        HashMap result = this.orderDao.searchDriverTodayBusinessData(driverId);

        // 封装距离
        String duration = MapUtil.getStr(result, "duration");
        if (duration == null) {
            duration = "0";
        }
        result.replace("duration", duration);

        // 封装收入
        String income = MapUtil.getStr(result, "income");
        if (income == null) {
            income = "0.00";
        }
        result.replace("income", income);

        return result;
    }
}

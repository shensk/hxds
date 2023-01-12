package com.aomsir.hxds.odr.service;

import com.aomsir.hxds.odr.db.pojo.OrderBillEntity;
import com.aomsir.hxds.odr.db.pojo.OrderEntity;

import java.util.HashMap;
import java.util.Map;

public interface OrderService {
    public HashMap searchDriverTodayBusinessData(long driverId);

    public String insertOrder(OrderEntity orderEntity, OrderBillEntity billEntity);
    public String acceptNewOrder(long driverId, long orderId);

    public HashMap searchDriverExecuteOrder(Map param);
}

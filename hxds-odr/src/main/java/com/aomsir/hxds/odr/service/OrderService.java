package com.aomsir.hxds.odr.service;

import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.odr.db.pojo.OrderBillEntity;
import com.aomsir.hxds.odr.db.pojo.OrderEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface OrderService {
    public HashMap searchDriverTodayBusinessData(long driverId);

    public String insertOrder(OrderEntity orderEntity, OrderBillEntity billEntity);
    public String acceptNewOrder(long driverId, long orderId);

    public HashMap searchDriverExecuteOrder(Map param);

    public Integer searchOrderStatus(Map param);
    public String deleteUnAcceptOrder(Map param);

    public HashMap searchDriverCurrentOrder(long driverId);
    public HashMap hasCustomerCurrentOrder(long customerId);

    public HashMap searchOrderForMoveById(Map param);

    public int arriveStartPlace(Map param);

    public boolean confirmArriveStartPlace(long orderId);

    public int startDriving(Map param);

    public int updateOrderStatus(Map param);

    public PageUtils searchOrderByPage(Map param);

    public HashMap searchOrderContent(long orderId);

    public ArrayList<HashMap> searchOrderStartLocationIn30Days();

    public boolean validDriverOwnOrder(Map param);

    public HashMap searchSettlementNeedData(long orderId);

    public HashMap searchOrderById(Map param);

    public HashMap validCanPayOrder(Map param);

    public int updateOrderPrepayId(Map param);
}

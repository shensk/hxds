package com.aomsir.hxds.odr.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.odr.db.dao.OrderBillDao;
import com.aomsir.hxds.odr.db.dao.OrderDao;
import com.aomsir.hxds.odr.db.pojo.OrderBillEntity;
import com.aomsir.hxds.odr.db.pojo.OrderEntity;
import com.aomsir.hxds.odr.service.OrderService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderBillDao orderBillDao;

    @Resource
    private RedisTemplate redisTemplate;
    
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


    @Override
    @Transactional
    @LcnTransaction
    public String insertOrder(OrderEntity orderEntity, OrderBillEntity billEntity) {

        int rows = this.orderDao.insert(orderEntity);   // 插入订单记录,返回影响的行数
        if (rows == 1) {
            String id = this.orderDao.searchOrderIdByUUID(orderEntity.getUuid());     // 根据UUID查询订单ID
            //插入订单费用记录
            billEntity.setOrderId(Long.parseLong(id));
            rows = this.orderBillDao.insert(billEntity);    // 插入订单账单记录
            if (rows == 1) {
                //往Redis里面插入缓存，配合Redis事务用于司机抢单，避免多个司机同时抢单成功
                this.redisTemplate.opsForValue().set("order#" + id, "none");
                this.redisTemplate.expire("order#" + id, 15, TimeUnit.MINUTES);   //缓存15分钟
                return id;
            } else {
                throw new HxdsException("保存新订单费用失败");
            }
        } else {
            throw new HxdsException("保存新订单失败");
        }
    }
}

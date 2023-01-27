package com.aomsir.hxds.odr.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.odr.db.dao.OrderBillDao;
import com.aomsir.hxds.odr.db.dao.OrderDao;
import com.aomsir.hxds.odr.db.pojo.OrderBillEntity;
import com.aomsir.hxds.odr.db.pojo.OrderEntity;
import com.aomsir.hxds.odr.service.OrderService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
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

    @Override
    @Transactional
    @LcnTransaction
    public String acceptNewOrder(long driverId, long orderId) {
        //Redis不存在抢单的新订单就代表抢单失败
        if (!this.redisTemplate.hasKey("order#" + orderId)) {
            return "抢单失败";
        }
        //执行Redis事务
        this.redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //获取新订单记录的Version
                operations.watch("order#" + orderId);
                //本地缓存Redis操作
                operations.multi();
                //把新订单缓存的Value设置成抢单司机的ID
                operations.opsForValue().set("order#" + orderId, driverId);
                //执行Redis事务，如果事务提交失败会自动抛出异常
                return operations.exec();

            }
        });
        //抢单成功之后，删除Redis中的新订单，避免让其他司机参与抢单
        this.redisTemplate.delete("order#" + orderId);
        //更新订单记录，添加上接单司机ID和接单时间
        HashMap param = new HashMap() {{
            put("driverId", driverId);
            put("orderId", orderId);
        }};
        int rows = orderDao.acceptNewOrder(param);
        if (rows != 1) {
            throw new HxdsException("接单失败，无法更新订单记录");
        }
        return "接单成功";
    }


    @Override
    public HashMap searchDriverExecuteOrder(Map param) {
        HashMap map = orderDao.searchDriverExecuteOrder(param);
        return map;
    }


    @Override
    public Integer searchOrderStatus(Map param) {
        Integer status = this.orderDao.searchOrderStatus(param);
        if (status == null) {
            // throw new HxdsException("没有查询到数据，请核对查询条件");
            status = 0;
        }
        return status;
    }

    @Override
    @Transactional
    @LcnTransaction
    public String deleteUnAcceptOrder(Map param) {
        long orderId = MapUtil.getLong(param, "orderId");
        if (!this.redisTemplate.hasKey("order#" + orderId)) {
            return "订单取消失败";
        }
        this.redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.watch("order#" + orderId);
                operations.multi();
                operations.opsForValue().set("order#" + orderId, "none");
                return operations.exec();
            }
        });

        this.redisTemplate.delete("order#" + orderId);
        int rows = this.orderDao.deleteUnAcceptOrder(param);
        if (rows != 1) {
            return "订单取消失败";
        }
        rows = this.orderBillDao.deleteUnAcceptOrderBill(orderId);
        if (rows != 1) {
            return "订单取消失败";
        }
        return "订单取消成功";
    }


    @Override
    public HashMap searchDriverCurrentOrder(long driverId) {
        HashMap map = this.orderDao.searchDriverCurrentOrder(driverId);
        return map;
    }


    @Override
    public HashMap hasCustomerCurrentOrder(long customerId) {
        HashMap result = new HashMap();
        HashMap map = this.orderDao.hasCustomerUnAcceptOrder(customerId);
        result.put("hasCustomerUnAcceptOrder", map != null);
        result.put("unAcceptOrder", map);
        Long id = this.orderDao.hasCustomerUnFinishedOrder(customerId);
        result.put("hasCustomerUnFinishedOrder", id != null);
        result.put("unFinishedOrder", id);
        return result;
    }

    @Override
    public HashMap searchOrderForMoveById(Map param) {
        HashMap map = this.orderDao.searchOrderForMoveById(param);
        return map;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int arriveStartPlace(Map param) {
        //添加到达上车点标志位
        long orderId = MapUtil.getLong(param, "orderId");
        this.redisTemplate.opsForValue().set("order_driver_arrived#" + orderId, "1");
        int rows = this.orderDao.updateOrderStatus(param);
        if (rows != 1) {
            throw new HxdsException("更新订单状态失败");
        }
        return rows;
    }


    @Override
    public boolean confirmArriveStartPlace(long orderId) {
        String key = "order_driver_arrived#" + orderId;
        if (this.redisTemplate.hasKey(key)
                && this.redisTemplate.opsForValue().get(key).toString().endsWith("1")) {
            this.redisTemplate.opsForValue().set(key, "2");
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int startDriving(Map param) {
        long orderId = MapUtil.getLong(param, "orderId");
        String key = "order_driver_arrived#" + orderId;
        if (this.redisTemplate.hasKey(key)
                && redisTemplate.opsForValue().get(key).toString().endsWith("2")) {
            this.redisTemplate.delete(key);
            int rows = orderDao.updateOrderStatus(param);
            if (rows != 1) {
                throw new HxdsException("更新订单状态失败");
            }
            return rows;
        }
        return 0;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int updateOrderStatus(Map param) {
        int rows = this.orderDao.updateOrderStatus(param);
        if (rows != 1) {
            throw new HxdsException("更新取消订单记录失败");
        }
        return rows;
    }

    @Override
    public PageUtils searchOrderByPage(Map param) {
        long count = this.orderDao.searchOrderCount(param);
        ArrayList<HashMap> list = null;
        if (count == 0) {
            list = new ArrayList<>();
        } else {
            list = this.orderDao.searchOrderByPage(param);
        }
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public HashMap searchOrderContent(long orderId) {
        HashMap map = this.orderDao.searchOrderContent(orderId);
        JSONObject startPlaceLocation = JSONUtil.parseObj(MapUtil.getStr(map, "startPlaceLocation"));
        JSONObject endPlaceLocation = JSONUtil.parseObj(MapUtil.getStr(map, "endPlaceLocation"));

        map.replace("startPlaceLocation", startPlaceLocation);
        map.replace("endPlaceLocation", endPlaceLocation);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchOrderStartLocationIn30Days() {
        ArrayList<String> list = this.orderDao.searchOrderStartLocationIn30Days();
        ArrayList<HashMap> result = new ArrayList<>();
        list.forEach(location -> {
            JSONObject json = JSONUtil.parseObj(location);
            String latitude = json.getStr("latitude");
            String longitude = json.getStr("longitude");
            latitude = latitude.substring(0, latitude.length() - 4);
            latitude += "0001";
            longitude = longitude.substring(0, longitude.length() - 4);
            longitude += "0001";
            HashMap map = new HashMap();
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            result.add(map);
        });
        return result;
    }

    @Override
    public boolean validDriverOwnOrder(Map param) {
        long count = this.orderDao.validDriverOwnOrder(param);
        return count == 1 ? true : false;
    }

    @Override
    public HashMap searchSettlementNeedData(long orderId) {
        HashMap map = this.orderDao.searchSettlementNeedData(orderId);
        return map;
    }

    @Override
    public HashMap searchOrderById(Map param) {
        HashMap map = this.orderDao.searchOrderById(param);
        String startPlaceLocation=MapUtil.getStr(map,"startPlaceLocation");
        String endPlaceLocation=MapUtil.getStr(map,"endPlaceLocation");
        map.replace("startPlaceLocation",JSONUtil.parseObj(startPlaceLocation));
        map.replace("endPlaceLocation",JSONUtil.parseObj(endPlaceLocation));
        return map;
    }
}

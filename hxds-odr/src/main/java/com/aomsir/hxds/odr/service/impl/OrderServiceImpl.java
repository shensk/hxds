package com.aomsir.hxds.odr.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.common.wxpay.MyWXPayConfig;
import com.aomsir.hxds.common.wxpay.WXPay;
import com.aomsir.hxds.common.wxpay.WXPayConfig;
import com.aomsir.hxds.common.wxpay.WXPayUtil;
import com.aomsir.hxds.odr.controller.form.TransferForm;
import com.aomsir.hxds.odr.db.dao.OrderBillDao;
import com.aomsir.hxds.odr.db.dao.OrderDao;
import com.aomsir.hxds.odr.db.pojo.OrderBillEntity;
import com.aomsir.hxds.odr.db.pojo.OrderEntity;
import com.aomsir.hxds.odr.feign.DrServiceApi;
import com.aomsir.hxds.odr.quartz.QuartzUtil;
import com.aomsir.hxds.odr.quartz.job.HandleProfitsharingJob;
import com.aomsir.hxds.odr.service.OrderService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

    @Resource
    private DrServiceApi drServiceApi;

    @Resource
    private QuartzUtil quartzUtil;

    @Resource
    private MyWXPayConfig myWXPayConfig;

    @Resource
    private WXPayConfig wxPayConfig;
    
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

    @Override
    public HashMap validCanPayOrder(Map param) {
        HashMap map = this.orderDao.validCanPayOrder(param);
        if (map == null || map.size() == 0) {
            throw new HxdsException("订单无法支付");
        }
        return map;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int updateOrderPrepayId(Map param) {
        int rows = this.orderDao.updateOrderPrepayId(param);
        if (rows != 1) {
            throw new HxdsException("更新预支付订单ID失败");
        }
        return rows;
    }

    @Override
    @Transactional
    @LcnTransaction
    public void handlePayment(String uuid, String payId, String driverOpenId, String payTime) {
        /*
         * 更新订单状态之前，先查询订单的状态。
         * 因为乘客端付款成功之后，会主动发起Ajax请求，要求更新订单状态。
         * 所以后端接收到付款通知消息之后，不要着急修改订单状态，先看一下订单是否已经是7状态
         */
        HashMap map = this.orderDao.searchOrderIdAndStatus(uuid);
        int status = MapUtil.getInt(map, "status");
        if (status == 7) {
            return;
        }

        HashMap param = new HashMap() {{
            put("uuid", uuid);
            put("payId", payId);
            put("payTime", payTime);
        }};
        //更新订单记录的PayId、状态和付款时间
        int rows = this.orderDao.updateOrderPayIdAndStatus(param);
        if (rows != 1) {
            throw new HxdsException("更新支付订单ID失败");
        }

        //查询系统奖励
        map = this.orderDao.searchDriverIdAndIncentiveFee(uuid);
        String incentiveFee = MapUtil.getStr(map, "incentiveFee");
        long driverId = MapUtil.getLong(map, "driverId");
        //判断系统奖励费是否大于0
        if (new BigDecimal(incentiveFee).compareTo(new BigDecimal("0.00")) == 1) {
            TransferForm form = new TransferForm();
            form.setUuid(IdUtil.simpleUUID());
            form.setAmount(incentiveFee);
            form.setDriverId(driverId);
            form.setType((byte) 2);
            form.setRemark("系统奖励费");
            //给司机钱包转账奖励费
            this.drServiceApi.transfer(form);
        }

        // 执行分账
        //先判断是否有分账定时器
        if (this.quartzUtil.checkExists(uuid, "代驾单分账任务组") || this.quartzUtil.checkExists(uuid, "查询代驾单分账任务组")) {
            //存在分账定时器就不需要再执行分账
            return;
        }
        //执行分账
        JobDetail jobDetail = JobBuilder.newJob(HandleProfitsharingJob.class).build();
        Map dataMap = jobDetail.getJobDataMap();
        dataMap.put("uuid", uuid);
        dataMap.put("driverOpenId", driverOpenId);
        dataMap.put("payId", payId);

        //2分钟之后执行分账定时器
        Date executeDate = new DateTime().offset(DateField.MINUTE, 2);
        this.quartzUtil.addJob(jobDetail, uuid, "代驾单分账任务组", executeDate);

        //更新订单状态为已完成状态（8）
        rows = this.orderDao.finishOrder(uuid);
        if (rows != 1) {
            throw new HxdsException("更新订单结束状态失败");
        }
    }



    @Override
    @Transactional
    @LcnTransaction
    public String updateOrderAboutPayment(Map param) {
        long orderId = MapUtil.getLong(param, "orderId");
        /*
         * 查询订单状态。
         * 因为有可能Web方法先收到了付款结果通知消息，把订单状态改成了7、8状态，
         * 所以我们要先查询订单状态。
         */
        HashMap map = this.orderDao.searchUuidAndStatus(orderId);
        String uuid = MapUtil.getStr(map, "uuid");
        int status = MapUtil.getInt(map, "status");
        //如果订单状态已经是已付款，就退出当前方法
        if (status == 7 || status == 8) {
            return "付款成功";
        }

        //查询支付结果的参数
        map.clear();
        map.put("appid", this.myWXPayConfig.getAppID());
        map.put("mch_id", this.myWXPayConfig.getMchID());
        map.put("out_trade_no", uuid);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //生成数字签名
            String sign = WXPayUtil.generateSignature(map, this.myWXPayConfig.getKey());
            map.put("sign", sign);

            WXPay wxPay = new WXPay(this.wxPayConfig);
            //查询支付结果
            Map<String, String> result = wxPay.orderQuery(map);

            String returnCode = result.get("return_code");
            String resultCode = result.get("result_code");
            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                String tradeState = result.get("trade_state");
                if ("SUCCESS".equals(tradeState)) {
                    String driverOpenId = result.get("attach");
                    String payId = result.get("transaction_id");
                    String payTime = new DateTime(result.get("time_end"), "yyyyMMddHHmmss").toString("yyyy-MM-dd HH:mm:ss");
                    //更新订单相关付款信息和状态
                    param.put("payId", payId);
                    param.put("payTime", payTime);

                    //把订单更新成7状态
                    int rows = this.orderDao.updateOrderAboutPayment(param);
                    if (rows != 1) {
                        throw new HxdsException("更新订单相关付款信息失败");
                    }

                    //查询系统奖励
                    map = this.orderDao.searchDriverIdAndIncentiveFee(uuid);
                    String incentiveFee = MapUtil.getStr(map, "incentiveFee");
                    long driverId = MapUtil.getLong(map, "driverId");
                    //判断系统奖励费是否大于0
                    if (new BigDecimal(incentiveFee).compareTo(new BigDecimal("0.00")) == 1) {
                        TransferForm form = new TransferForm();
                        form.setUuid(IdUtil.simpleUUID());
                        form.setAmount(incentiveFee);
                        form.setDriverId(driverId);
                        form.setType((byte) 2);
                        form.setRemark("系统奖励费");
                        //给司机钱包转账奖励费
                        this.drServiceApi.transfer(form);
                    }

                    //先判断是否有分账定时器
                    if (this.quartzUtil.checkExists(uuid, "代驾单分账任务组") || this.quartzUtil.checkExists(uuid, "查询代驾单分账任务组")) {
                        //存在分账定时器就不需要再执行分账
                        return "付款成功";
                    }
                    //执行分账
                    JobDetail jobDetail = JobBuilder.newJob(HandleProfitsharingJob.class).build();
                    Map dataMap = jobDetail.getJobDataMap();
                    dataMap.put("uuid", uuid);
                    dataMap.put("driverOpenId", driverOpenId);
                    dataMap.put("payId", payId);

                    Date executeDate = new DateTime().offset(DateField.MINUTE, 2);
                    this.quartzUtil.addJob(jobDetail, uuid, "代驾单分账任务组", executeDate);
                    rows = this.orderDao.finishOrder(uuid);
                    if(rows!=1){
                        throw new HxdsException("更新订单结束状态失败");
                    }
                    return "付款成功";
                } else {
                    return "付款异常";
                }
            } else {
                return "付款异常";
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HxdsException("更新订单相关付款信息失败");
        }
    }
}

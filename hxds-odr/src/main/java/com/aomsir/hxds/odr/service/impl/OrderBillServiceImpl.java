package com.aomsir.hxds.odr.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.odr.db.dao.OrderBillDao;
import com.aomsir.hxds.odr.db.dao.OrderDao;
import com.aomsir.hxds.odr.db.dao.OrderProfitsharingDao;
import com.aomsir.hxds.odr.db.pojo.OrderProfitsharingEntity;
import com.aomsir.hxds.odr.service.OrderBillService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderBillServiceImpl implements OrderBillService {

    @Resource
    private OrderBillDao orderBillDao;

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderProfitsharingDao profitsharingDao;

    @Override
    @Transactional
    @LcnTransaction
    public int updateBillFee(Map param) {
        //更新账单数据
        int rows = this.orderBillDao.updateBillFee(param);
        if (rows != 1) {
            throw new HxdsException("更新账单费用详情失败");
        }

        //更新订单数据
        rows = this.orderDao.updateOrderMileageAndFee(param);
        if (rows != 1) {
            throw new HxdsException("更新订单费用详情失败");
        }

        //添加分账单数据
        OrderProfitsharingEntity entity = new OrderProfitsharingEntity();
        entity.setOrderId(MapUtil.getLong(param, "orderId"));
        entity.setRuleId(MapUtil.getLong(param, "ruleId"));
        entity.setAmountFee(new BigDecimal((String) param.get("total")));
        entity.setPaymentRate(new BigDecimal((String) param.get("paymentRate")));
        entity.setPaymentFee(new BigDecimal((String) param.get("paymentFee")));
        entity.setTaxRate(new BigDecimal((String) param.get("taxRate")));
        entity.setTaxFee(new BigDecimal((String) param.get("taxFee")));
        entity.setSystemIncome(new BigDecimal((String) param.get("systemIncome")));
        entity.setDriverIncome(new BigDecimal((String) param.get("driverIncome")));
        rows = this.profitsharingDao.insert(entity);
        if (rows != 1) {
            throw new HxdsException("添加分账记录失败");
        }
        return rows;
    }


    @Override
    public HashMap searchReviewDriverOrderBill(Map param) {
        HashMap map = this.orderBillDao.searchReviewDriverOrderBill(param);
        return map;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int updateBillPayment(Map param) {
        int rows = this.orderBillDao.updateBillPayment(param);
        if (rows != 1) {
            throw new HxdsException("更新账单实际支付费用失败");
        }
        return rows;
    }
}

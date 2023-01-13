package com.aomsir.hxds.bff.driver.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.driver.controller.form.*;
import com.aomsir.hxds.bff.driver.feign.CstServiceApi;
import com.aomsir.hxds.bff.driver.feign.OdrServiceApi;
import com.aomsir.hxds.bff.driver.service.OrderService;
import com.aomsir.hxds.common.util.R;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OdrServiceApi odrServiceApi;

    @Resource
    private CstServiceApi cstServiceApi;

    @Override
    @LcnTransaction
    @Transactional
    public String acceptNewOrder(AcceptNewOrderForm form) {
        R r = this.odrServiceApi.acceptNewOrder(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }

    @Override
    public HashMap searchDriverExecuteOrder(SearchDriverExecuteOrderForm form) {
        //查询订单信息
        R r = odrServiceApi.searchDriverExecuteOrder(form);
        HashMap orderMap = (HashMap) r.get("result");

        //查询代驾客户信息
        long customerId = MapUtil.getLong(orderMap, "customerId");
        SearchCustomerInfoInOrderForm infoInOrderForm = new SearchCustomerInfoInOrderForm();
        infoInOrderForm.setCustomerId(customerId);
        r = this.cstServiceApi.searchCustomerInfoInOrder(infoInOrderForm);
        HashMap cstMap = (HashMap) r.get("result");

        HashMap map = new HashMap();
        map.putAll(orderMap);
        map.putAll(cstMap);
        return map;
    }

    @Override
    public HashMap searchDriverCurrentOrder(SearchDriverCurrentOrderForm form) {
        R r = this.odrServiceApi.searchDriverCurrentOrder(form);
        HashMap orderMap = (HashMap) r.get("result");

        if (MapUtil.isNotEmpty(orderMap)) {
            HashMap map = new HashMap();
            //查询代驾客户信息
            long customerId = MapUtil.getLong(orderMap, "customerId");
            SearchCustomerInfoInOrderForm infoInOrderForm = new SearchCustomerInfoInOrderForm();
            infoInOrderForm.setCustomerId(customerId);
            r = this.cstServiceApi.searchCustomerInfoInOrder(infoInOrderForm);
            HashMap cstMap = (HashMap) r.get("result");
            map.putAll(orderMap);
            map.putAll(cstMap);
            return map;
        } else {
            return null;
        }
    }

    @Override
    public HashMap searchOrderForMoveById(SearchOrderForMoveByIdForm form) {
        R r = this.odrServiceApi.searchOrderForMoveById(form);
        HashMap map = (HashMap) r.get("result");
        return map;
    }
}

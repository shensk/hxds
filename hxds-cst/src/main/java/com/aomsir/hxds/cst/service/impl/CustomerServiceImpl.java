package com.aomsir.hxds.cst.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.MicroAppUtil;
import com.aomsir.hxds.cst.db.dao.CustomerDao;
import com.aomsir.hxds.cst.service.CustomerService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Aomsir
 * @Date: 2023/1/9
 * @Description:
 * @Email: info@say521.cn
 * @GitHub: https://github.com/aomsir
 */

@Service
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerDao customerDao;

    @Resource
    private MicroAppUtil microAppUtil;

    @Override
    @Transactional
    @LcnTransaction
    public String registerNewCustomer(Map param) {
        String code = MapUtil.getStr(param, "code");   // 获取微信临时授权ID
        String openId = this.microAppUtil.getOpenId(code);  // 根据微信临时授权ID获取openId

        HashMap tempMap = new HashMap(){{
            put("openId", openId);
        }};

        if (this.customerDao.hasCustomer(tempMap) != 0) {
            throw new HxdsException("该微信无法注册");
        }

        param.put("openId", openId);
        this.customerDao.registerNewCustomer(param);
        String customerId = this.customerDao.searchCustomerId(openId);

        return customerId;
    }


    @Override
    public String login(String code) {
        String openId = this.microAppUtil.getOpenId(code);
        String customerId = this.customerDao.login(openId);
        customerId = (customerId != null ? customerId : "");
        return customerId;
    }


    @Override
    public HashMap searchCustomerInfoInOrder(long customerId) {
        HashMap map = this.customerDao.searchCustomerInfoInOrder(customerId);
        return map;
    }

    @Override
    public HashMap searchCustomerBriefInfo(long customerId) {
        HashMap map = this.customerDao.searchCustomerBriefInfo(customerId);
        return map;
    }

    @Override
    public String searchCustomerOpenId(long customerId) {
        String openId = this.customerDao.searchCustomerOpenId(customerId);
        return openId;
    }
}

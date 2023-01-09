package com.aomsir.hxds.bff.customer.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.aomsir.hxds.bff.customer.controller.form.LoginForm;
import com.aomsir.hxds.bff.customer.controller.form.RegisterNewCustomerForm;
import com.aomsir.hxds.bff.customer.feign.CstServiceApi;
import com.aomsir.hxds.bff.customer.service.CustomerService;
import com.aomsir.hxds.common.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: Aomsir
 * @Date: 2023/1/9
 * @Description:
 * @Email: info@say521.cn
 * @GitHub: https://github.com/aomsir
 */

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);
    @Resource
    private CstServiceApi cstServiceApi;

    @Override
    public long registerNewCustomer(RegisterNewCustomerForm form) {

        R r = this.cstServiceApi.registerNewCustomer(form);
        Long userId = Convert.toLong(r.get("userId"));
        return userId;
    }

    @Override
    public Long login(LoginForm form) {
        R login = this.cstServiceApi.login(form);
        String userId = MapUtil.getStr(login, "userId");

        if (StrUtil.isNotBlank(userId)){
            return Convert.toLong(userId);
        }
        return null;
    }
}

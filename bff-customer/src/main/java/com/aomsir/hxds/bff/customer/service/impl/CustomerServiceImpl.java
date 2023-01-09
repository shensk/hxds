package com.aomsir.hxds.bff.customer.service.impl;

import cn.hutool.core.convert.Convert;
import com.aomsir.hxds.bff.customer.controller.form.RegisterNewCustomerForm;
import com.aomsir.hxds.bff.customer.feign.CstServiceApi;
import com.aomsir.hxds.bff.customer.service.CustomerService;
import com.aomsir.hxds.common.util.R;

import javax.annotation.Resource;

/**
 * @Author: Aomsir
 * @Date: 2023/1/9
 * @Description:
 * @Email: info@say521.cn
 * @GitHub: https://github.com/aomsir
 */
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CstServiceApi cstServiceApi;

    @Override
    public long registerNewCustomer(RegisterNewCustomerForm form) {

        R r = this.cstServiceApi.registerNewCustomer(form);
        Long userId = Convert.toLong(r.get("userId"));
        return userId;
    }
}

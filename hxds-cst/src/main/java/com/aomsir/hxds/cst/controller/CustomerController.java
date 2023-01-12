package com.aomsir.hxds.cst.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.cst.controller.form.LoginForm;
import com.aomsir.hxds.cst.controller.form.RegisterNewCustomerForm;
import com.aomsir.hxds.cst.controller.form.SearchCustomerInfoInOrderForm;
import com.aomsir.hxds.cst.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@Tag(name = "CustomerController", description = "客户Web接口")
public class CustomerController {
    @Resource
    private CustomerService customerService;

    @PostMapping("/registerNewCustomer")
    @Operation(summary = "注册新乘客")
    public R registerNewCustomer(@RequestBody @Valid RegisterNewCustomerForm form) {
        Map param = BeanUtil.beanToMap(form);   // 将json转换为Map
        String userId = this.customerService.registerNewCustomer(param);
        return R.ok()
                .put("userId", userId);
    }


    @PostMapping("/login")
    @Operation(summary = "登陆系统")
    public R login(@RequestBody @Valid LoginForm form) {
        String userId = this.customerService.login(form.getCode());
        return R.ok()
                .put("userId", userId);
    }

    @PostMapping("/searchCustomerInfoInOrder")
    @Operation(summary = "查询订单中的客户信息")
    public R searchCustomerInfoInOrder(@RequestBody @Valid SearchCustomerInfoInOrderForm form) {
        HashMap map = customerService.searchCustomerInfoInOrder(form.getCustomerId());
        return R.ok()
                .put("result", map);
    }
}

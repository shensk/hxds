package com.aomsir.hxds.cst.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.cst.controller.form.RegisterNewCustomerForm;
import com.aomsir.hxds.cst.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
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
}

package com.aomsir.hxds.bff.customer.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aomsir.hxds.bff.customer.controller.form.LoginForm;
import com.aomsir.hxds.bff.customer.controller.form.RegisterNewCustomerForm;
import com.aomsir.hxds.bff.customer.service.CustomerService;
import com.aomsir.hxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/customer")
@Tag(name = "CustomerController", description = "客户Web接口")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    @Resource
    private CustomerService customerService;

    @PostMapping("/registerNewCustomer")
    @Operation(summary = "注册新司机")
    public R registerNewCustomer(@RequestBody @Valid RegisterNewCustomerForm form) {
        long customerId = this.customerService.registerNewCustomer(form);

        StpUtil.login(customerId);
        String token = StpUtil.getTokenInfo().getTokenValue();

        return R.ok()
                .put("token", token);
    }

    @PostMapping("/login")
    @Operation(summary = "登陆系统")
    public R login(@RequestBody @Valid LoginForm form) {
        Long customerId = this.customerService.login(form);
        if (customerId != null) {
            StpUtil.login(customerId);
            String token = StpUtil.getTokenInfo().getTokenValue();
            return R.ok()
                    .put("token", token);
        }
        return R.ok();
    }
}

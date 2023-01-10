package com.aomsir.hxds.bff.customer.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aomsir.hxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.aomsir.hxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchCustomerCarListForm;
import com.aomsir.hxds.bff.customer.service.CustomerCarService;
import com.aomsir.hxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/customer/car")
@Tag(name = "CustomerController", description = "客户车辆Web接口")
public class CustomerCarController {
    @Resource
    private CustomerCarService customerCarService;

    @PostMapping("/insertCustomerCar")
    @Operation(summary = "添加客户车辆")
    public R insertCustomerCar(@RequestBody @Valid InsertCustomerCarForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        this.customerCarService.insertCustomerCar(form);
        return R.ok();
    }

    @PostMapping("/searchCustomerCarList")
    @Operation(summary = "查询客户车辆列表")
    public R searchCustomerCarList(@RequestBody @Valid SearchCustomerCarListForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        ArrayList<HashMap> list = this.customerCarService.searchCustomerCarList(form);
        return R.ok()
                .put("result", list);
    }

    @PostMapping("/deleteCustomerCarById")
    @Operation(summary = "删除客户车辆")
    public R deleteCustomerCarById(@RequestBody @Valid DeleteCustomerCarByIdForm form) {
        int rows = this.customerCarService.deleteCustomerCarById(form);
        return R.ok()
                .put("rows", rows);
    }
}

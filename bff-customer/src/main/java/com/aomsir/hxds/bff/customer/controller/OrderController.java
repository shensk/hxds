package com.aomsir.hxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.aomsir.hxds.bff.customer.controller.form.CreateNewOrderForm;
import com.aomsir.hxds.bff.customer.controller.form.DeleteUnAcceptOrderForm;
import com.aomsir.hxds.bff.customer.controller.form.HasCustomerCurrentOrderForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchOrderStatusForm;
import com.aomsir.hxds.bff.customer.service.OrderService;
import com.aomsir.hxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/order")
@Tag(name = "OrderController", description = "订单模块Web接口")
public class OrderController {
    @Resource
    private OrderService orderService;

    @PostMapping("/createNewOrder")
    @Operation(summary = "创建新订单")
    @SaCheckLogin
    public R createNewOrder(@RequestBody @Valid CreateNewOrderForm form){
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        HashMap result = this.orderService.createNewOrder(form);
        return R.ok()
                .put("result",result);
    }

    @PostMapping("/searchOrderStatus")
    @Operation(summary = "查询订单状态")
    @SaCheckLogin
    public R searchOrderStatus(@RequestBody @Valid SearchOrderStatusForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Integer status = this.orderService.searchOrderStatus(form);
        return R.ok()
                .put("result", status);
    }

    @PostMapping("/deleteUnAcceptOrder")
    @Operation(summary = "关闭没有司机接单的订单")
    @SaCheckLogin
    public R deleteUnAcceptOrder(@RequestBody @Valid DeleteUnAcceptOrderForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);

        String result = this.orderService.deleteUnAcceptOrder(form);
        return R.ok()
                .put("result", result);
    }

    @PostMapping("/hasCustomerCurrentOrder")
    @SaCheckLogin
    @Operation(summary = "查询乘客是否存在当前的订单")
    public R hasCustomerCurrentOrder() {
        long customerId = StpUtil.getLoginIdAsLong();
        HasCustomerCurrentOrderForm form=new HasCustomerCurrentOrderForm();
        form.setCustomerId(customerId);
        HashMap map = this.orderService.hasCustomerCurrentOrder(form);
        return R.ok()
                .put("result", map);
    }
}


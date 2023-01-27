package com.aomsir.hxds.odr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aomsir.hxds.bff.customer.controller.form.UpdateBillPaymentForm;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.odr.controller.form.SearchReviewDriverOrderBillForm;
import com.aomsir.hxds.odr.controller.form.UpdateBillFeeForm;
import com.aomsir.hxds.odr.service.OrderBillService;
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
@RequestMapping("/bill")
@Tag(name = "OrderBillController", description = "订单费用账单Web接口")
public class OrderBillController {
    @Resource
    private OrderBillService orderBillService;
    
    @PostMapping("/updateBillFee")
    @Operation(summary = "更新订单账单费用")
    public R updateBillFee(@RequestBody @Valid UpdateBillFeeForm form) {
        Map param = BeanUtil.beanToMap(form);
        int rows = this.orderBillService.updateBillFee(param);
        return R.ok()
                .put("rows", rows);
    }


    @PostMapping("/searchReviewDriverOrderBill")
    @Operation(summary = "查询司机预览账单")
    public R searchReviewDriverOrderBill(@RequestBody @Valid SearchReviewDriverOrderBillForm form) {
        Map param = BeanUtil.beanToMap(form);
        HashMap map = this.orderBillService.searchReviewDriverOrderBill(param);
        return R.ok()
                .put("result", map);
    }

    @PostMapping("/updateBillPayment")
    @Operation(summary = "更新账单实际支付费用")
    public R updateBillPayment(@RequestBody @Valid UpdateBillPaymentForm form) {
        Map param = BeanUtil.beanToMap(form);
        int rows = this.orderBillService.updateBillPayment(param);
        return R.ok()
                .put("rows", rows);
    }
}

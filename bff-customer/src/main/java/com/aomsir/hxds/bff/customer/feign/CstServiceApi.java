package com.aomsir.hxds.bff.customer.feign;

import com.aomsir.hxds.bff.customer.controller.form.RegisterNewCustomerForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-cst")
public interface CstServiceApi {

    @PostMapping("/customer/registerNewCustomer")
    public R registerNewCustomer(RegisterNewCustomerForm form);
}

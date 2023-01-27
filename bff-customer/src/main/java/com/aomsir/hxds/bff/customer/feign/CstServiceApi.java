package com.aomsir.hxds.bff.customer.feign;

import com.aomsir.hxds.bff.customer.controller.form.*;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-cst")
public interface CstServiceApi {

    @PostMapping("/customer/registerNewCustomer")
    public R registerNewCustomer(RegisterNewCustomerForm form);

    @PostMapping("/customer/login")
    public R login(LoginForm form);

    @PostMapping("/customer/car/insertCustomerCar")
    public R insertCustomerCar(InsertCustomerCarForm form);

    @PostMapping("/customer/car/searchCustomerCarList")
    public R searchCustomerCarList(SearchCustomerCarListForm form);

    @PostMapping("/customer/car/deleteCustomerCarById")
    public R deleteCustomerCarById(DeleteCustomerCarByIdForm form);

    @PostMapping("/customer/searchCustomerOpenId")
    public R searchCustomerOpenId(SearchCustomerOpenIdForm form);
}

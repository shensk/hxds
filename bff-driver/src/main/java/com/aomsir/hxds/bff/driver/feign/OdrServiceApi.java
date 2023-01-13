package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.controller.form.AcceptNewOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchDriverCurrentOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchDriverExecuteOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchDriverTodayBusinessDataForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-odr")
public interface OdrServiceApi {
    
    @PostMapping("/order/searchDriverTodayBusinessData")
    public R searchDriverTodayBusinessData(SearchDriverTodayBusinessDataForm form);

    @PostMapping("/order/acceptNewOrder")
    public R acceptNewOrder(AcceptNewOrderForm form);

    @PostMapping("/order/searchDriverExecuteOrder")
    public R searchDriverExecuteOrder(SearchDriverExecuteOrderForm form);

    @PostMapping("/order/searchDriverCurrentOrder")
    public R searchDriverCurrentOrder(SearchDriverCurrentOrderForm form);
}

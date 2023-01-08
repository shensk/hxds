package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.controller.form.SearchDriverTodayBusinessDataForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-odr")
public interface OdrServiceApi {
    
    @PostMapping("/order/searchDriverTodayBusinessData")
    public R searchDriverTodayBusinessData(SearchDriverTodayBusinessDataForm form);
}

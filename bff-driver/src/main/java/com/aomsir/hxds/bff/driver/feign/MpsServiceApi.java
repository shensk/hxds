package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-mps")
public interface MpsServiceApi {

    @PostMapping("/driver/location/updateLocationCache")
    public R updateLocationCache(UpdateLocationCacheForm form);

    @PostMapping("/driver/location/removeLocationCache")
    public R removeLocationCache(RemoveLocationCacheForm form);
}


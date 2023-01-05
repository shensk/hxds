package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.controller.form.RegisterNewDriverForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-dr")  // Nacos服务注册名
public interface DrServiceApi {

    @PostMapping("/driver/registerNewDriver")
    public R registerNewDriver(RegisterNewDriverForm form);
}

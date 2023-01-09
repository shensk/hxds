package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.controller.form.*;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-dr")  // Nacos服务注册名
public interface DrServiceApi {

    @PostMapping("/driver/registerNewDriver")
    public R registerNewDriver(RegisterNewDriverForm form);

    @PostMapping("/driver/updateDriverAuth")
    public R updateDriverAuth(UpdateDriverAuthForm form);

    @PostMapping("/driver/createDriverFaceModel")
    public R createDriverFaceModel(CreateDriverFaceModelForm form);

    @PostMapping("/driver/login")
    public R login(LoginForm form);

    @PostMapping("/driver/searchDriverBaseInfo")
    public R searchDriverBaseInfo(SearchDriverBaseInfoForm form);

    @PostMapping("/driver/settings/searchDriverSettings")
    public R searchDriverSettings(SearchDriverSettingsForm form);

    @PostMapping("/driver/searchDriverAuth")
    public R searchDriverAuth(SearchDriverAuthForm form);
}

package com.aomsir.hxds.bff.driver.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aomsir.hxds.bff.driver.controller.form.RegisterNewDriverForm;
import com.aomsir.hxds.bff.driver.service.DriverService;
import com.aomsir.hxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/driver")
@Tag(name = "DriverController", description = "司机模块Web接口")
public class DriverController {
    @Resource
    private DriverService driverService;

    @PostMapping("/registerNewDriver")
    @Operation(summary = "新司机注册")
    public R registerNewDriver(@RequestBody @Valid RegisterNewDriverForm form) {
        long driverId = this.driverService.registerNewDriver(form);

        StpUtil.login(driverId);     //在SaToken上面执行登陆,实际上就是缓存userId,然后才有资格拿到令牌
        String token = StpUtil.getTokenInfo().getTokenValue();    //生成Token令牌字符串（已加密）
        return R.ok()
                .put("token", token);
    }
}

package com.aomsir.hxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.driver.controller.form.CreateDriverFaceModelForm;
import com.aomsir.hxds.bff.driver.controller.form.LoginForm;
import com.aomsir.hxds.bff.driver.controller.form.RegisterNewDriverForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateDriverAuthForm;
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
import java.util.HashMap;

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

    @PostMapping("/updateDriverAuth")
    @Operation(summary = "更新实名认证信息")
    @SaCheckLogin
    public R updateDriverAuth(@RequestBody @Valid UpdateDriverAuthForm form) {
        long driverId = StpUtil.getLoginIdAsLong();   // 从SaToken中获取driverId
        form.setDriverId(driverId);
        int rows = this.driverService.updateDriverAuth(form);
        return R.ok()
                .put("rows", rows);
    }


    @PostMapping("/createDriverFaceModel")
    @Operation(summary = "创建司机人脸模型归档")
    @SaCheckLogin
    public R createDriverFaceModel(@RequestBody @Valid CreateDriverFaceModelForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        String result = this.driverService.createDriverFaceModel(form);
        return R.ok()
                .put("result", result);
    }


    @PostMapping("/login")
    @Operation(summary = "登陆系统")
    public R login(@RequestBody @Valid LoginForm form) {
        HashMap map = this.driverService.login(form);
        if (map != null) {
            long driverId = MapUtil.getLong(map, "id");
            byte realAuth = Byte.parseByte(MapUtil.getStr(map, "realAuth"));
            boolean archive = MapUtil.getBool(map, "archive");

            StpUtil.login(driverId);
            String token = StpUtil.getTokenInfo().getTokenValue();

            return R.ok()
                    .put("token", token)
                    .put("realAuth", realAuth)
                    .put("archive", archive);
        }
        return R.ok();
    }
}

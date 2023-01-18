package com.aomsir.hxds.dr.controller;


import cn.hutool.core.bean.BeanUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.dr.controller.form.*;
import com.aomsir.hxds.dr.service.DriverService;
import com.aomsir.hxds.dr.service.DriverSettingsService;
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
@RequestMapping("/driver")
@Tag(name = "DriverController", description = "司机模块Web接口")
public class DriverController {
    @Resource
    private DriverService driverService;


    @Resource
    private DriverSettingsService driverSettingsService;
    
	@PostMapping("/registerNewDriver")
    @Operation(summary = "新司机注册")
    public R registerNewDriver(@RequestBody @Valid RegisterNewDriverForm form) {
        Map param = BeanUtil.beanToMap(form);    // 只能转换为Map,所以dao参数就都是Map
        String userId = this.driverService.registerNewDriver(param);
        return R.ok()
                .put("userId", userId);
    }

    @PostMapping("/updateDriverAuth")
    @Operation(summary = "更新实名认证信息")
    public R updateDriverAuth(@RequestBody @Valid UpdateDriverAuthForm form) {
        Map param = BeanUtil.beanToMap(form);   // JSON转Map
        int rows = this.driverService.updateDriverAuth(param);

        return R.ok()
                .put("rows", rows);
    }

    @PostMapping("/createDriverFaceModel")
    @Operation(summary = "创建司机人脸模型归档")
    public R createDriverFaceModel(@RequestBody @Valid CreateDriverFaceModelForm form) {
        String result = this.driverService.createDriverFaceModel(form.getDriverId(), form.getPhoto());
        return R.ok()
                .put("result", result);
    }


    @PostMapping("/login")
    @Operation(summary = "登陆系统")
    public R login(@RequestBody @Valid LoginForm form) {

        // code是bff系统传递过来的临时ID
        HashMap map = this.driverService.login(form.getCode());
        return R.ok()
                .put("result", map);
    }


    @PostMapping("/searchDriverBaseInfo")
    @Operation(summary = "查询司机基本信息")
    public R searchDriverBaseInfo(@RequestBody @Valid SearchDriverBaseInfoForm form) {
        HashMap result = this.driverService.searchDriverBaseInfo(form.getDriverId());
        return R.ok()
                .put("result", result);
    }

    @PostMapping("/settings/searchDriverSettings")
    @Operation(summary = "查询司机的设置")
    public R searchDriverSettings(@RequestBody @Valid SearchDriverSettingsForm form) {
        HashMap map = this.driverSettingsService.searchDriverSettings(form.getDriverId());
        return R.ok()
                .put("result", map);
    }

    @PostMapping("/searchDriverByPage")
    @Operation(summary = "查询司机分页记录")
    public R searchDriverByPage(@RequestBody @Valid SearchDriverByPageForm form) {
        Map param = BeanUtil.beanToMap(form);
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        param.put("start", start);
        PageUtils pageUtils = this.driverService.searchDriverByPage(param);
        return R.ok()
                .put("result", pageUtils);
    }

    @PostMapping("/searchDriverAuth")
    @Operation(summary = "查询司机认证信息")
    public R searchDriverAuth(@RequestBody @Valid SearchDriverAuthForm form) {
        HashMap result = this.driverService.searchDriverAuth(form.getDriverId());
        return R.ok()
                .put("result", result);
    }


    @PostMapping("/searchDriverRealSummary")
    @Operation(summary = "查询司机实名信息摘要")
    public R searchDriverRealSummary(@RequestBody @Valid SearchDriverRealSummaryForm form) {
        HashMap map = this.driverService.searchDriverRealSummary(form.getDriverId());
        return R.ok()
                .put("result", map);
    }

    @PostMapping("/updateDriverRealAuth")
    @Operation(summary = "更新司机实名认证状态")
    public R updateDriverRealAuth(@RequestBody @Valid UpdateDriverRealAuthForm form) {
        Map param = BeanUtil.beanToMap(form);
        int rows = this.driverService.updateDriverRealAuth(param);
        return R.ok().put("rows", rows);
    }


    @PostMapping("/searchDriverBriefInfo")
    @Operation(summary = "查询司机简明信息")
    public R searchDriverBriefInfo(@RequestBody @Valid SearchDriverBriefInfoForm form) {
        HashMap map = this.driverService.searchDriverBriefInfo(form.getDriverId());
        return R.ok()
                .put("result", map);
    }
}

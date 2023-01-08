package com.aomsir.hxds.bff.driver.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.driver.controller.form.*;
import com.aomsir.hxds.bff.driver.feign.DrServiceApi;
import com.aomsir.hxds.bff.driver.feign.OdrServiceApi;
import com.aomsir.hxds.bff.driver.service.DriverService;
import com.aomsir.hxds.common.util.R;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class DriverServiceImpl implements DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);
    @Resource
    private DrServiceApi drServiceApi;   // Driver Feign接口


    @Resource
    private OdrServiceApi odrServiceApi;


    @Override
    @Transactional
    @LcnTransaction
    public long registerNewDriver(RegisterNewDriverForm form) {
        log.info("{}",form);
        R r = this.drServiceApi.registerNewDriver(form);  // 调用远程方法,获取封装了userID的R对象
        long userId = Convert.toLong(r.get("userId"));
        return userId;
    }


    @Override
    @Transactional
    @LcnTransaction
    public int updateDriverAuth(UpdateDriverAuthForm form) {
        R r = this.drServiceApi.updateDriverAuth(form);
        int rows = Convert.toInt(r.get("rows"));
        return rows;
    }


    @Override
    @Transactional
    @LcnTransaction
    public String createDriverFaceModel(CreateDriverFaceModelForm form) {
        R r = this.drServiceApi.createDriverFaceModel(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }


    @Override
    public HashMap login(LoginForm form) {
        R r = this.drServiceApi.login(form);
        HashMap map = (HashMap) r.get("result");
        return map;
    }


    @Override
    public HashMap searchDriverBaseInfo(SearchDriverBaseInfoForm form) {
        R r = this.drServiceApi.searchDriverBaseInfo(form);
        HashMap map = (HashMap) r.get("result");
        return map;
    }


    @Override
    public HashMap searchWorkbenchData(long driverId) {
        //查询司机当天业务数据
        SearchDriverTodayBusinessDataForm form_1 = new SearchDriverTodayBusinessDataForm();
        form_1.setDriverId(driverId);
        R r = this.odrServiceApi.searchDriverTodayBusinessData(form_1);
        HashMap business = (HashMap) r.get("result");

        //查询司机的设置
        SearchDriverSettingsForm form_2 = new SearchDriverSettingsForm();
        form_2.setDriverId(driverId);
        r = this.drServiceApi.searchDriverSettings(form_2);
        HashMap settings = (HashMap) r.get("result");
        HashMap result = new HashMap() {{
            put("business", business);
            put("settings", settings);
        }};
        return result;
    }
}

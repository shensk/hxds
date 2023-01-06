package com.aomsir.hxds.bff.driver.service.impl;

import cn.hutool.core.convert.Convert;
import com.aomsir.hxds.bff.driver.controller.form.RegisterNewDriverForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateDriverAuthForm;
import com.aomsir.hxds.bff.driver.feign.DrServiceApi;
import com.aomsir.hxds.bff.driver.service.DriverService;
import com.aomsir.hxds.common.util.R;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class DriverServiceImpl implements DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);
    @Resource
    private DrServiceApi drServiceApi;   // Driver Feign接口

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
}

package com.aomsir.hxds.bff.driver.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.aomsir.hxds.bff.driver.feign.NebulaServiceApi;
import com.aomsir.hxds.bff.driver.service.OrderGpsService;
import com.aomsir.hxds.common.util.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderGpsServiceImpl implements OrderGpsService {
    @Resource
    private NebulaServiceApi nebulaServiceApi;

    @Override
    public int insertOrderGps(InsertOrderGpsForm form) {
        R r = this.nebulaServiceApi.insertOrderGps(form);
        int rows = MapUtil.getInt(r, "rows");
        return rows;
    }
}

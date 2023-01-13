package com.aomsir.hxds.bff.driver.service.impl;

import com.aomsir.hxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;
import com.aomsir.hxds.bff.driver.feign.MpsServiceApi;
import com.aomsir.hxds.bff.driver.service.DriverLocationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DriverLocationServiceImpl implements DriverLocationService {
    @Resource
    private MpsServiceApi mpsServiceApi;

    @Override
    public void updateLocationCache(UpdateLocationCacheForm form) {
        this.mpsServiceApi.updateLocationCache(form);
    }

    @Override
    public void removeLocationCache(RemoveLocationCacheForm form) {
        this.mpsServiceApi.removeLocationCache(form);
    }

    @Override
    public void updateOrderLocationCache(UpdateOrderLocationCacheForm form) {
        this.mpsServiceApi.updateOrderLocationCache(form);
    }
}

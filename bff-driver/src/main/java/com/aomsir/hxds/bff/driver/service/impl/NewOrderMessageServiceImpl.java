package com.aomsir.hxds.bff.driver.service.impl;

import com.aomsir.hxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.aomsir.hxds.bff.driver.feign.SnmServiceApi;
import com.aomsir.hxds.bff.driver.service.NewOrderMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NewOrderMessageServiceImpl implements NewOrderMessageService {
    @Resource
    private SnmServiceApi snmServiceApi;
    
    @Override
    public void clearNewOrderQueue(ClearNewOrderQueueForm form) {
        this.snmServiceApi.clearNewOrderQueue(form);
    }
}

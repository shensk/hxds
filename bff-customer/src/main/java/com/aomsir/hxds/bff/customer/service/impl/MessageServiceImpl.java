package com.aomsir.hxds.bff.customer.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.customer.controller.form.ReceiveBillMessageForm;
import com.aomsir.hxds.bff.customer.feign.SnmServiceApi;
import com.aomsir.hxds.bff.customer.service.MessageService;
import com.aomsir.hxds.common.util.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private SnmServiceApi snmServiceApi;

    @Override
    public String receiveBillMessage(ReceiveBillMessageForm form) {
        R r = this.snmServiceApi.receiveBillMessage(form);
        String msg = MapUtil.getStr(r, "result");
        return msg;
    }
}

package com.aomsir.hxds.bff.customer.feign;

import com.aomsir.hxds.bff.customer.controller.form.SendNewOrderMessageForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-snm")
public interface SnmServiceApi {
    @PostMapping("/message/order/new/sendNewOrderMessageAsync")
    public R sendNewOrderMessageAsync(SendNewOrderMessageForm form);
}

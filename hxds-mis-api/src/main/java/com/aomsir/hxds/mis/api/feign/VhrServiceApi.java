package com.aomsir.hxds.mis.api.feign;

import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.SearchVoucherByPageForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-vhr")
public interface VhrServiceApi {

    @PostMapping("/voucher/searchVoucherByPage")
    public R searchVoucherByPage(SearchVoucherByPageForm form);
}

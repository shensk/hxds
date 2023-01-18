package com.aomsir.hxds.mis.api.feign;

import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.SearchOrderByPageForm;
import com.aomsir.hxds.mis.api.controller.form.SearchOrderContentForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-odr")
public interface OdrServiceApi {
    
    @PostMapping("/order/searchOrderByPage")
    public R searchOrderByPage(SearchOrderByPageForm form);

    @PostMapping("/order/searchOrderContent")
    public R searchOrderContent(SearchOrderContentForm form);
}


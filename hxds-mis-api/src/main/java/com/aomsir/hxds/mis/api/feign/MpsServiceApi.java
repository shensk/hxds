package com.aomsir.hxds.mis.api.feign;

import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.CalculateDriveLineForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-mps")
public interface MpsServiceApi {
    
    @PostMapping("/map/calculateDriveLine")
    public R calculateDriveLine(CalculateDriveLineForm form);
}

package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.config.MultipartSupportConfig;
import com.aomsir.hxds.bff.driver.controller.form.InsertOrderGpsForm;
import com.aomsir.hxds.bff.driver.controller.form.InsertOrderMonitoringForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "hxds-nebula", configuration = MultipartSupportConfig.class)
public interface NebulaServiceApi {
    
    @PostMapping(value = "/monitoring/uploadRecordFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R uploadRecordFile(@RequestPart(value = "file") MultipartFile file,
                              @RequestPart("name") String name,
                              @RequestPart(value = "text", required = false) String text);

    @PostMapping(value = "/monitoring/insertOrderMonitoring")
    public R insertOrderMonitoring(InsertOrderMonitoringForm form);

    @PostMapping("/order/gps/insertOrderGps")
    public R insertOrderGps(InsertOrderGpsForm form);
}

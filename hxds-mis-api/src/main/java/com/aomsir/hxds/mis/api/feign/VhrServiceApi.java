package com.aomsir.hxds.mis.api.feign;

import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-vhr")
public interface VhrServiceApi {

    @PostMapping("/voucher/searchVoucherByPage")
    public R searchVoucherByPage(SearchVoucherByPageForm form);

    @PostMapping("/voucher/insertVoucher")
    public R insertVoucher(InsertVoucherForm form);

    @PostMapping("/voucher/updateVoucherStatus")
    public R updateVoucherStatus(UpdateVoucherStatusForm form);

    @PostMapping("/voucher/deleteVoucherByIds")
    public R deleteVoucherByIds(DeleteVoucherByIdsForm form);
}

package com.aomsir.hxds.mis.api.service;

import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.mis.api.controller.form.InsertVoucherForm;
import com.aomsir.hxds.mis.api.controller.form.SearchVoucherByPageForm;

public interface VoucherService {
    public PageUtils searchVoucherByPage(SearchVoucherByPageForm form);

    public int insertVoucher(InsertVoucherForm form);
}

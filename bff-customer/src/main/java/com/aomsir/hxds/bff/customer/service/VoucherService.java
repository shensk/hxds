package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.SearchUnTakeVoucherByPageForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchUnUseVoucherByPageForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchUnUseVoucherCountForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchUsedVoucherByPageForm;
import com.aomsir.hxds.common.util.PageUtils;

public interface VoucherService {
    
    public PageUtils searchUnTakeVoucherByPage(SearchUnTakeVoucherByPageForm form);
    
    public PageUtils searchUnUseVoucherByPage(SearchUnUseVoucherByPageForm form);
    
    public PageUtils searchUsedVoucherByPage(SearchUsedVoucherByPageForm form);

    public long searchUnUseVoucherCount(SearchUnUseVoucherCountForm form);

}

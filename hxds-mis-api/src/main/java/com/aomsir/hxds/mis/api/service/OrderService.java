package com.aomsir.hxds.mis.api.service;

import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.mis.api.controller.form.SearchOrderByPageForm;

public interface OrderService {
    public PageUtils searchOrderByPage(SearchOrderByPageForm form);
}

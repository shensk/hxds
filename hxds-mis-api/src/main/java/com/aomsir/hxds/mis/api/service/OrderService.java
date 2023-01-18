package com.aomsir.hxds.mis.api.service;

import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.mis.api.controller.form.SearchOrderByPageForm;

import java.util.HashMap;

public interface OrderService {
    public PageUtils searchOrderByPage(SearchOrderByPageForm form);

    public HashMap searchOrderComprehensiveInfo(long orderId);
}

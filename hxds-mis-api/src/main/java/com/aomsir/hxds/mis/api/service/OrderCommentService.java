package com.aomsir.hxds.mis.api.service;

import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.mis.api.controller.form.SearchCommentByPageForm;

public interface OrderCommentService {
    public PageUtils searchCommentByPage(SearchCommentByPageForm form);
}

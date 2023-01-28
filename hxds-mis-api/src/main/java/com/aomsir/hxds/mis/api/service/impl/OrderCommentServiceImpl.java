package com.aomsir.hxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.SearchCommentByPageForm;
import com.aomsir.hxds.mis.api.feign.OdrServiceApi;
import com.aomsir.hxds.mis.api.service.OrderCommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class OrderCommentServiceImpl implements OrderCommentService {
    @Resource
    private OdrServiceApi odrServiceApi;

    @Override
    public PageUtils searchCommentByPage(SearchCommentByPageForm form) {
        R r = this.odrServiceApi.searchCommentByPage(form);
        HashMap map = (HashMap) r.get("result");
        PageUtils pageUtils = BeanUtil.toBean(map, PageUtils.class);
        return pageUtils;
    }
}

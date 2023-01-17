package com.aomsir.hxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.SearchOrderByPageForm;
import com.aomsir.hxds.mis.api.feign.OdrServiceApi;
import com.aomsir.hxds.mis.api.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OdrServiceApi odrServiceApi;
    
    @Override
    public PageUtils searchOrderByPage(SearchOrderByPageForm form) {
        R r = this.odrServiceApi.searchOrderByPage(form);
        HashMap map = (HashMap) r.get("result");
        PageUtils pageUtils = BeanUtil.toBean(map, PageUtils.class);
        return pageUtils;
    }
}

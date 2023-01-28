package com.aomsir.hxds.bff.customer.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.customer.controller.form.InsertCommentForm;
import com.aomsir.hxds.bff.customer.feign.OdrServiceApi;
import com.aomsir.hxds.bff.customer.service.OrderCommentService;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.R;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class OrderCommentServiceImpl implements OrderCommentService {

    @Resource
    private OdrServiceApi odrServiceApi;

    @Override
    @Transactional
    @LcnTransaction
    public int insertComment(InsertCommentForm form) {
        R r = this.odrServiceApi.insertComment(form);
        int rows = MapUtil.getInt(r, "rows");
        if (rows != 1) {
            throw new HxdsException("保存订单评价失败");
        }
        return rows;
    }
}

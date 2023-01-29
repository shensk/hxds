package com.aomsir.hxds.mis.api.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.InsertVoucherForm;
import com.aomsir.hxds.mis.api.controller.form.SearchVoucherByPageForm;
import com.aomsir.hxds.mis.api.controller.form.UpdateVoucherStatusForm;
import com.aomsir.hxds.mis.api.feign.VhrServiceApi;
import com.aomsir.hxds.mis.api.service.VoucherService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Resource
    private VhrServiceApi vhrServiceApi;

    @Override
    public PageUtils searchVoucherByPage(SearchVoucherByPageForm form) {
        R r = this.vhrServiceApi.searchVoucherByPage(form);
        HashMap map = (HashMap) r.get("result");
        PageUtils pageUtils = BeanUtil.toBean(map, PageUtils.class);
        return pageUtils;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int insertVoucher(InsertVoucherForm form) {
        R r = this.vhrServiceApi.insertVoucher(form);
        int rows = MapUtil.getInt(r, "rows");
        return rows;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int updateVoucherStatus(UpdateVoucherStatusForm form) {
        R r = this.vhrServiceApi.updateVoucherStatus(form);
        int rows = MapUtil.getInt(r, "rows");
        return rows;
    }
}

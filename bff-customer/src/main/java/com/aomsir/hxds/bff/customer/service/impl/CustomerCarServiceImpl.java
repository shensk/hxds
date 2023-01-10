package com.aomsir.hxds.bff.customer.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.aomsir.hxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchCustomerCarListForm;
import com.aomsir.hxds.bff.customer.feign.CstServiceApi;
import com.aomsir.hxds.bff.customer.service.CustomerCarService;
import com.aomsir.hxds.common.util.R;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class CustomerCarServiceImpl implements CustomerCarService {
    @Resource
    private CstServiceApi cstServiceApi;

    @Override
    @Transactional
    @LcnTransaction
    public void insertCustomerCar(InsertCustomerCarForm form) {
        this.cstServiceApi.insertCustomerCar(form);
    }

    @Override
    public ArrayList<HashMap> searchCustomerCarList(SearchCustomerCarListForm form) {
        R r = this.cstServiceApi.searchCustomerCarList(form);
        ArrayList<HashMap> list = (ArrayList<HashMap>) r.get("result");
        return list;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int deleteCustomerCarById(DeleteCustomerCarByIdForm form) {
        R r = this.cstServiceApi.deleteCustomerCarById(form);
        int rows = MapUtil.getInt(r, "rows");
        return rows;
    }
}

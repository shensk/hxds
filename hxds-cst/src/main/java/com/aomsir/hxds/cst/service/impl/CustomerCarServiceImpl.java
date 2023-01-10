package com.aomsir.hxds.cst.service.impl;

import com.aomsir.hxds.cst.db.dao.CustomerCarDao;
import com.aomsir.hxds.cst.db.pojo.CustomerCarEntity;
import com.aomsir.hxds.cst.service.CustomerCarService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class CustomerCarServiceImpl implements CustomerCarService {
    @Resource
    private CustomerCarDao customerCarDao;

    @Override
    @Transactional
    @LcnTransaction
    public void insertCustomerCar(CustomerCarEntity entity) {
        this.customerCarDao.insert(entity);
    }

    @Override
    public ArrayList<HashMap> searchCustomerCarList(long customerId) {
        ArrayList list = this.customerCarDao.searchCustomerCarList(customerId);
        return list;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int deleteCustomerCarById(long id) {
        int rows = this.customerCarDao.deleteCustomerCarById(id);
        return rows;
    }
}

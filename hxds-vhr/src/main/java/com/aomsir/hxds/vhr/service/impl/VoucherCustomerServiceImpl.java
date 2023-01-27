package com.aomsir.hxds.vhr.service.impl;

import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.vhr.db.dao.VoucherCustomerDao;
import com.aomsir.hxds.vhr.service.VoucherCustomerService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class VoucherCustomerServiceImpl implements VoucherCustomerService {

    @Resource
    private VoucherCustomerDao voucherCustomerDao;

    @Override
    @Transactional
    @LcnTransaction
    public String useVoucher(Map param) {
        String discount = this.voucherCustomerDao.validCanUseVoucher(param);
        if (discount != null) {
            int rows = this.voucherCustomerDao.bindVoucher(param);
            if (rows != 1) {
                throw new HxdsException("代金券不可用");
            }
            return discount;
        }
        throw new HxdsException("代金券不可用");
    }
}

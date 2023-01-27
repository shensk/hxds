package com.aomsir.hxds.dr.service.impl;

import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.dr.db.dao.WalletDao;
import com.aomsir.hxds.dr.db.dao.WalletIncomeDao;
import com.aomsir.hxds.dr.db.pojo.WalletIncomeEntity;
import com.aomsir.hxds.dr.service.WalletIncomeService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
@Slf4j
public class WalletIncomeServiceImpl implements WalletIncomeService {

    @Resource
    private WalletIncomeDao walletIncomeDao;

    @Resource
    private WalletDao walletDao;

    @Override
    @Transactional
    @LcnTransaction
    public int transfer(WalletIncomeEntity entity) {
        //添加转账记录
        int rows = this.walletIncomeDao.insert(entity);
        if (rows != 1) {
            throw new HxdsException("添加转账记录失败");
        }

        HashMap param = new HashMap() {{
            put("driverId", entity.getDriverId());
            put("amount", entity.getAmount());
        }};

        //更新帐户余额
        rows = this.walletDao.updateWalletBalance(param);
        if (rows != 1) {
            throw new HxdsException("更新钱包余额失败");
        }
        return rows;
    }
}

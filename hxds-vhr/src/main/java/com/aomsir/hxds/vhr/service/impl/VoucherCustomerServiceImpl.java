package com.aomsir.hxds.vhr.service.impl;

import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.vhr.db.dao.VoucherCustomerDao;
import com.aomsir.hxds.vhr.db.dao.VoucherDao;
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

    @Resource
    private VoucherDao voucherDao;

    @Override
    @Transactional
    @LcnTransaction
    public String useVoucher(Map param) {
        String discount = this.voucherCustomerDao.validCanUseVoucher(param);
        if(discount != null){
            int rows = this.voucherCustomerDao.bindVoucher(param);
            if(rows!=1){
                throw new HxdsException("使用代金券失败");
            }

            //新添加内容
            long voucherId = MapUtil.getLong(param, "voucherId");
            rows = this.voucherDao.updateUsedCount(voucherId);
            if(rows!=1){
                throw new HxdsException("更新代金券使用数量失败");
            }

            return discount;
        }
        throw new HxdsException("代金券不可用");
    }
}

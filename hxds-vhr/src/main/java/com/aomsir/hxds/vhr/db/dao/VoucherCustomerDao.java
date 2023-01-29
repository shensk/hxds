package com.aomsir.hxds.vhr.db.dao;


import com.aomsir.hxds.vhr.db.pojo.VoucherCustomerEntity;

import java.util.Map;

public interface VoucherCustomerDao {
    public int insert(VoucherCustomerEntity entity);

    public String validCanUseVoucher(Map param);

    public int bindVoucher(Map param);

    public long searcherTakeVoucherNum(Map param);
}





package com.aomsir.hxds.vhr.service;

import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.vhr.db.pojo.VoucherEntity;

import java.util.Map;

public interface VoucherService {
    public PageUtils searchVoucherByPage(Map param);

    public int insert(VoucherEntity entity);

    public int updateVoucherStatus(Map param);

    public int deleteVoucherByIds(Long[] ids);

    public PageUtils searchUnTakeVoucherByPage(Map param);

    public PageUtils searchUnUseVoucherByPage(Map param);

    public PageUtils searchUsedVoucherByPage(Map param);

    public long searchUnUseVoucherCount(Map param);
}

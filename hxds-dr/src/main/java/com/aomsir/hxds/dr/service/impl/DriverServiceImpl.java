package com.aomsir.hxds.dr.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.MicroAppUtil;
import com.aomsir.hxds.dr.db.dao.DriverDao;
import com.aomsir.hxds.dr.db.dao.DriverSettingsDao;
import com.aomsir.hxds.dr.db.dao.WalletDao;
import com.aomsir.hxds.dr.db.pojo.DriverSettingsEntity;
import com.aomsir.hxds.dr.db.pojo.WalletEntity;
import com.aomsir.hxds.dr.service.DriverService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService {
    @Resource
    private MicroAppUtil microAppUtil;
    
    @Resource
    private DriverDao driverDao;

    @Resource
    private DriverSettingsDao settingsDao;
    
    @Resource
    private WalletDao walletDao;
    
    @Override
    @Transactional
    @LcnTransaction
    public String registerNewDriver(Map param) {

        String code = MapUtil.getStr(param, "code");   // 获取临时授权字符串
        String openId = this.microAppUtil.getOpenId(code);  // 兑换成永久ID

        HashMap tempMap = new HashMap(){{
           put("openId", openId);
        }};
        if (this.driverDao.hasDriver(tempMap) != 0) {
            throw new HxdsException("该微信无法注册");
        }

        param.put("openId", openId);
        this.driverDao.registerNewDriver(param);    // 没有注册,进行注册
        String driverId = this.driverDao.searchDriverId(openId);   // 获取司机ID

        // 设置司机默认属性
        JSONObject json = new JSONObject();
        json.set("orientation", "");
        json.set("listenService", true);
        json.set("orderDistance", 0);
        json.set("rangeDistance", 5);
        json.set("autoAccept", false);
        DriverSettingsEntity settingsEntity = new DriverSettingsEntity();
        settingsEntity.setSettings(json.toString());
        settingsEntity.setDriverId(Long.parseLong(driverId));
        this.settingsDao.insertDriverSettings(settingsEntity);

        // 设置自己钱包属性
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setDriverId(Long.parseLong(driverId));
        walletEntity.setBalance(new BigDecimal("0"));
        walletEntity.setPassword(null);     // 支付密码为空，司机支付的时候,系统会自动提示设置支付密码
        this.walletDao.insert(walletEntity);

        return driverId;
    }
}

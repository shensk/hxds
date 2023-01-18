package com.aomsir.hxds.dr.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.MicroAppUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.dr.db.dao.DriverDao;
import com.aomsir.hxds.dr.db.dao.DriverSettingsDao;
import com.aomsir.hxds.dr.db.dao.WalletDao;
import com.aomsir.hxds.dr.db.pojo.DriverSettingsEntity;
import com.aomsir.hxds.dr.db.pojo.WalletEntity;
import com.aomsir.hxds.dr.service.DriverService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService {

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.face.groupName}")
    private String groupName;

    @Value("${tencent.cloud.face.region}")
    private String region;

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
        String openId = this.microAppUtil.getOpenId(code);  // 兑换成永久openId

        HashMap tempMap = new HashMap(){{
           put("openId", openId);
        }};
        if (this.driverDao.hasDriver(tempMap) != 0) {
            throw new HxdsException("该微信无法注册");
        }

        param.put("openId", openId);
        this.driverDao.registerNewDriver(param);    // 没有注册,进行注册
        String driverId = this.driverDao.searchDriverId(openId);   // 获取司机ID、用户登陆

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


    @Override
    @Transactional
    @LcnTransaction
    public int updateDriverAuth(Map param) {
        int rows = this.driverDao.updateDriverAuth(param);
        return rows;
    }


    @Override
    @Transactional
    @LcnTransaction
    public String createDriverFaceModel(long driverId, String photo) {
        HashMap map = this.driverDao.searchDriverNameAndSex(driverId);
        String name = MapUtil.getStr(map, "name");
        String sex = MapUtil.getStr(map, "sex");


        // 创建人脸库进腾讯云
        Credential cred = new Credential(this.secretId, this.secretKey);
        IaiClient client = new IaiClient(cred, this.region);
        try {
            CreatePersonRequest request = new CreatePersonRequest();
            request.setGroupId(this.groupName);
            request.setPersonId(driverId+"");
            long gender = sex.equals("男") ? 1L : 2L;
            request.setGender(gender);
            request.setQualityControl(4L);
            request.setUniquePersonControl(4L);
            request.setPersonName(name);
            request.setImage(photo);
            CreatePersonResponse resp = client.CreatePerson(request);

            // 创建成功的执行逻辑
            if (StrUtil.isNotBlank(resp.getFaceId())) {
                int rows = this.driverDao.updateDriverArchive(driverId);
                if (rows != 1) {
                    return "更新司机归档字段失败";
                }
            }
        } catch (Exception e) {
            log.error("创建腾讯云端司机档案失败", e);
            return "创建腾讯云端司机档案失败";
        }
        return "";   // 返回null,JSON会将值为null的字段抹掉
    }


    @Override
    public HashMap login(String code) {
        String openId = microAppUtil.getOpenId(code);
        HashMap result = this.driverDao.login(openId);

        // 封装archive
        if (result != null && result.containsKey("archive")) {
            int temp = MapUtil.getInt(result, "archive");
            boolean archive = temp == 1 ? true : false;
            result.replace("archive", archive);
        }
        return result;
    }


    @Override
    public HashMap searchDriverBaseInfo(long driverId) {
        HashMap result = this.driverDao.searchDriverBaseInfo(driverId);
        JSONObject summary = JSONUtil.parseObj(MapUtil.getStr(result, "summary"));  // 将查询出的json字段转换为对象
        result.replace("summary", summary);
        return result;
    }


    @Override
    public PageUtils searchDriverByPage(Map param) {
        long count = this.driverDao.searchDriverCount(param);
        ArrayList<HashMap> list = null;
        if (count == 0) {
            list = new ArrayList<>();
        } else {
            list = this.driverDao.searchDriverByPage(param);
        }

        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);   // 封装分页对象
        return pageUtils;
    }


    @Override
    public HashMap searchDriverAuth(long id) {
        HashMap result = this.driverDao.searchDriverAuth(id);   // 根据司机ID查询司机认证信息
        return result;
    }

    @Override
    public HashMap searchDriverRealSummary(long driverId) {
        HashMap map = this.driverDao.searchDriverRealSummary(driverId);
        return map;
    }


    @Override
    @Transactional
    @LcnTransaction
    public int updateDriverRealAuth(Map param) {
        int rows = this.driverDao.updateDriverRealAuth(param);
        return rows;
    }

    @Override
    public HashMap searchDriverBriefInfo(long driverId) {
        HashMap map = this.driverDao.searchDriverBriefInfo(driverId);
        return map;
    }
}

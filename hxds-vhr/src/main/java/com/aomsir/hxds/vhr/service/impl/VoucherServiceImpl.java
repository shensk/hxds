package com.aomsir.hxds.vhr.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.vhr.db.dao.VoucherCustomerDao;
import com.aomsir.hxds.vhr.db.dao.VoucherDao;
import com.aomsir.hxds.vhr.db.pojo.VoucherEntity;
import com.aomsir.hxds.vhr.service.VoucherService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VoucherServiceImpl implements VoucherService {
    @Resource
    private VoucherDao voucherDao;
    
    @Resource
    private VoucherCustomerDao voucherCustomerDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public PageUtils searchVoucherByPage(Map param) {
        long count = this.voucherDao.searchVoucherCount(param);
        ArrayList<HashMap> list = null;
        if (count > 0) {
            list = this.voucherDao.searchVoucherByPage(param);
        } else {
            list = new ArrayList<>();
        }
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int insert(VoucherEntity entity) {
        entity.setTakeCount(0);
        entity.setUsedCount(0);
        int rows = this.voucherDao.insert(entity);
        return rows;
    }

    @Override
    @Transactional
    @LcnTransaction
    public int updateVoucherStatus(Map param) {
        int rows = this.voucherDao.updateVoucherStatus(param);
        if (rows == 1) {
            Long id = (Long) param.get("id");
            Byte status = (Byte) param.get("status");
            String uuid = (String) param.get("uuid");
            if (status == 1) {
                HashMap result = this.voucherDao.searchVoucherById(id);
                VoucherEntity entity = BeanUtil.toBean(result, VoucherEntity.class);
                //把代金券信息保存到缓存中
                this.saveVoucherCache(entity);
            } else if (status == 3) {
                //删除缓存的代金券
                this.redisTemplate.delete("voucher_info_" + uuid);
                this.redisTemplate.delete("voucher_" + uuid);
            }
        }
        return rows;
    }

    private void saveVoucherCache(VoucherEntity entity) {
        String uuid = entity.getUuid();
        HashMap map = new HashMap() {{
            put("totalQuota", entity.getTotalQuota());
            put("discount", entity.getDiscount());
            put("limitQuota", entity.getLimitQuota());
            put("type", entity.getType());
            put("withAmount", entity.getWithAmount());
            put("timeType", entity.getTimeType());
            put("startTime", entity.getStartTime());
            put("endTime", entity.getEndTime());
            put("days", entity.getDays());
        }};
        this.redisTemplate.opsForHash().putAll("voucher_info_" + uuid, map);
        this.redisTemplate.opsForValue().set("voucher_" + uuid, entity.getTotalQuota());
        //如果代金券有日期限制，就设置过期时间
        if (entity.getTimeType() != null && entity.getTimeType() == 2) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.parse(entity.getStartTime() + " 00:00:00", formatter);
            LocalDateTime endTime = LocalDateTime.parse(entity.getEndTime() + " 00:00:00", formatter);
            Duration duration = Duration.between(startTime, endTime);
            this.redisTemplate.expire("voucher_info_" + uuid, duration);
            this.redisTemplate.expire("voucher_" + uuid, duration);
        }
    }


    @Override
    @Transactional
    @LcnTransaction
    public int deleteVoucherByIds(Long[] ids) {
        ArrayList<HashMap> list = this.voucherDao.searchVoucherTakeCount(ids);
        ArrayList<Long> temp = new ArrayList();
        list.forEach(one -> {
            long id = MapUtil.getLong(one, "id");
            String uuid = MapUtil.getStr(one, "uuid");
            long totalQuota = MapUtil.getLong(one, "totalQuota");
            long takeCount = MapUtil.getLong(one, "takeCount");
            if (takeCount == 0) {
                //查询Redis中的缓存记录
                if (this.redisTemplate.hasKey("voucher_" + uuid)) {
                    long num = Long.parseLong(redisTemplate.opsForValue().get("voucher_" + uuid).toString());
                    //没有人领取代金券
                    if (num == totalQuota) {
                        temp.add(id);
                        //删除Redis缓存
                        this.redisTemplate.delete("voucher_" + uuid);
                        this.redisTemplate.delete("voucher_info_" + uuid);
                    } else {
                        log.debug("主键是" + id + "的代金券不能被删除");
                    }
                } else {
                    temp.add(id);
                }
            } else {
                //该记录不能删除
                log.debug("主键是" + id + "的代金券不能被删除");
            }
        });
        if (temp.size() > 0) {
            ids = temp.toArray(new Long[temp.size()]);
            int rows = this.voucherDao.deleteVoucherByIds(ids);
            return rows;
        }
        return 0;
    }


    @Override
    public PageUtils searchUnTakeVoucherByPage(Map param) {
        long count = this.voucherDao.searchUnTakeVoucherCount(param);
        ArrayList<HashMap> list = null;
        if (count > 0) {
            list = this.voucherDao.searchUnTakeVoucherByPage(param);
        } else {
            list = new ArrayList<>();
        }
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public PageUtils searchUnUseVoucherByPage(Map param) {
        long count = this.voucherDao.searchUnUseVoucherCount(param);
        ArrayList<HashMap> list = null;
        if (count > 0) {
            list = this.voucherDao.searchUnUseVoucherByPage(param);
        } else {
            list = new ArrayList<>();
        }
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public PageUtils searchUsedVoucherByPage(Map param) {
        long count = this.voucherDao.searchUsedVoucherCount(param);
        ArrayList<HashMap> list = null;
        if (count > 0) {
            list = this.voucherDao.searchUsedVoucherByPage(param);
        } else {
            list = new ArrayList<>();
        }
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public long searchUnUseVoucherCount(Map param) {
        long count = this.voucherDao.searchUnUseVoucherCount(param);
        return count;
    }

}

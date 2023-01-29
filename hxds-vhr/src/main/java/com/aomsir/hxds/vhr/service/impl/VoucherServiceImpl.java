package com.aomsir.hxds.vhr.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.vhr.db.dao.VoucherCustomerDao;
import com.aomsir.hxds.vhr.db.dao.VoucherDao;
import com.aomsir.hxds.vhr.db.pojo.VoucherCustomerEntity;
import com.aomsir.hxds.vhr.db.pojo.VoucherEntity;
import com.aomsir.hxds.vhr.service.VoucherService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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

    @Override
    @Transactional
    @LcnTransaction
    public boolean takeVoucher(Map param) {
        String uuid = MapUtil.getStr(param, "uuid");
        long id = MapUtil.getLong(param, "id");
        long customerId = MapUtil.getLong(param, "customerId");

        if (!(this.redisTemplate.hasKey("voucher_" + uuid) && this.redisTemplate.hasKey("voucher_info_" + uuid))) {
            return false;
        }

        //开启Redis事务，领取代金券
        boolean result = (Boolean) this.redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.watch("voucher_" + uuid);

                Map entries = operations.opsForHash().entries("voucher_info_" + uuid); //代金券信息
                int totalQuota = MapUtil.getInt(entries, "totalQuota"); //代金券总数
                short limitQuota = Short.parseShort(entries.get("limitQuota").toString()); //限制领取
                if (limitQuota > 0) {
                    HashMap condition = new HashMap() {{
                        put("customerId", customerId);
                        put("voucherId", id);
                    }};
                    //查询该乘客已经领取的代金券数量
                    long count = voucherCustomerDao.searcherTakeVoucherNum(condition);
                    if (count >= limitQuota) {
                        return false;
                    }
                }

                //领取代金券后的有效期
                String startTime = null;
                String endTime = null;
                if (entries.get("timeType") != null) {
                    byte timeType = Byte.parseByte(entries.get("timeType").toString()); //有效期类型
                    if (timeType == 1) {
                        int days = MapUtil.getInt(entries, "days");
                        startTime = DateUtil.today();
                        endTime = new DateTime().offset(DateField.DAY_OF_MONTH, days).toDateStr();
                    } else if (timeType == 2) {
                        startTime = MapUtil.getStr(entries, "startTime");
                        endTime = MapUtil.getStr(entries, "endTime");
                    }
                }
                VoucherCustomerEntity entity = new VoucherCustomerEntity();
                entity.setVoucherId(id);
                entity.setCustomerId(customerId);
                entity.setStartTime(startTime);
                entity.setEndTime(endTime);

                //代金券没有限量
                if (totalQuota == 0) {
                    int rows = voucherDao.takeVoucher(id); //更新代金券领取数量
                    if (rows == 1) {
                        rows = voucherCustomerDao.insert(entity); //记录领取的代金券
                        return rows == 1 ? true : false;
                    }
                    else {
                        return false;
                    }
                    // return rows == 1 ? true : false;
                }
                //代金券有数量上限
                else {
                    String temp = operations.opsForValue().get("voucher_" + uuid).toString();
                    int num = Integer.parseInt(temp);
                    if (num > 0) {
                        num--;
                        operations.multi();
                        //扣减Redis缓存
                        operations.opsForValue().set("voucher_" + uuid, num);
                        operations.exec();

                        int rows = voucherDao.takeVoucher(id); //更新代金券领取数量
                        if (rows == 1) {
                            rows = voucherCustomerDao.insert(entity); //记录领取的代金券
                            return rows == 1 ? true : false;
                        } else {
                            return false;
                        }
                    } else {
                        operations.unwatch();
                        //删除缓存
                        operations.delete("voucher_" + uuid);
                        operations.delete("voucher_info_" + uuid);
                        return false;
                    }
                }

            }
        });
        return result;
    }

    @Override
    public HashMap searchBestUnUseVoucher(Map param) {
        HashMap map = this.voucherDao.searchBestUnUseVoucher(param);
        return map;
    }
}

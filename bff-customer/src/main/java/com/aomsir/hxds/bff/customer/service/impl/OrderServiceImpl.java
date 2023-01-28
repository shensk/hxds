package com.aomsir.hxds.bff.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.aomsir.hxds.bff.customer.controller.form.*;
import com.aomsir.hxds.bff.customer.feign.*;
import com.aomsir.hxds.bff.customer.service.OrderService;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.PageUtils;
import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.common.wxpay.MyWXPayConfig;
import com.aomsir.hxds.common.wxpay.WXPay;
import com.aomsir.hxds.common.wxpay.WXPayUtil;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Resource
    private OdrServiceApi odrServiceApi;

    @Resource
    private MpsServiceApi mpsServiceApi;

    @Resource
    private RuleServiceApi ruleServiceApi;

    @Resource
    private SnmServiceApi snmServiceApi;

    @Resource
    private DrServiceApi drServiceApi;


    @Resource
    private MyWXPayConfig myWXPayConfig;

    @Resource
    private VhrServiceApi vhrServiceApi;

    @Resource
    private CstServiceApi cstServiceApi;

    @Override
    @Transactional
    @LcnTransaction
    public HashMap createNewOrder(CreateNewOrderForm form) {
        Long customerId = form.getCustomerId();
        String startPlace = form.getStartPlace();
        String startPlaceLatitude = form.getStartPlaceLatitude();
        String startPlaceLongitude = form.getStartPlaceLongitude();
        String endPlace = form.getEndPlace();
        String endPlaceLatitude = form.getEndPlaceLatitude();
        String endPlaceLongitude = form.getEndPlaceLongitude();
        String favourFee = form.getFavourFee();
        /**
         * 【重新预估里程和时间】
         * 虽然下单前，系统会预估里程和时长，但是有可能顾客在下单页面停留时间过长，
         * 然后再按下单键，这时候路线和时长可能都有变化，所以需要重新预估里程和时间
         */
        EstimateOrderMileageAndMinuteForm form_1 = new EstimateOrderMileageAndMinuteForm();
        form_1.setMode("driving");
        form_1.setStartPlaceLatitude(startPlaceLatitude);
        form_1.setStartPlaceLongitude(startPlaceLongitude);
        form_1.setEndPlaceLatitude(endPlaceLatitude);
        form_1.setEndPlaceLongitude(endPlaceLongitude);
        R r = this.mpsServiceApi.estimateOrderMileageAndMinute(form_1);     // 计算里程和时间
        HashMap map = (HashMap) r.get("result");
        String mileage = MapUtil.getStr(map, "mileage");
        int minute = MapUtil.getInt(map, "minute");

        /**
         * 重新估算订单金额
         */
        EstimateOrderChargeForm form_2 = new EstimateOrderChargeForm();
        form_2.setMileage(mileage);
        form_2.setTime(new DateTime().toTimeStr());
        r = this.ruleServiceApi.estimateOrderCharge(form_2);       // 计算订单金额
        map = (HashMap) r.get("result");
        String expectsFee = MapUtil.getStr(map, "amount");
        String chargeRuleId = MapUtil.getStr(map, "chargeRuleId");
        short baseMileage = MapUtil.getShort(map, "baseMileage");
        String baseMileagePrice = MapUtil.getStr(map, "baseMileagePrice");
        String exceedMileagePrice = MapUtil.getStr(map, "exceedMileagePrice");
        short baseMinute = MapUtil.getShort(map, "baseMinute");
        String exceedMinutePrice = MapUtil.getStr(map, "exceedMinutePrice");
        short baseReturnMileage = MapUtil.getShort(map, "baseReturnMileage");
        String exceedReturnPrice = MapUtil.getStr(map, "exceedReturnPrice");


        /*
         * 搜索适合接单的司机
         */
        SearchBefittingDriverAboutOrderForm form_3 = new SearchBefittingDriverAboutOrderForm();
        form_3.setStartPlaceLatitude(startPlaceLatitude);
        form_3.setStartPlaceLongitude(startPlaceLongitude);
        form_3.setEndPlaceLatitude(endPlaceLatitude);
        form_3.setEndPlaceLongitude(endPlaceLongitude);
        form_3.setMileage(mileage);
        r = this.mpsServiceApi.searchBefittingDriverAboutOrder(form_3);     // 获取可接单列表
        ArrayList<HashMap> list = (ArrayList<HashMap>) r.get("result");

        HashMap result = new HashMap() {{
            put("count", 0);
        }};

        if (list.size() > 0) {
            /**
             * 生成订单记录
             */
            InsertOrderForm form_4 = new InsertOrderForm();
            //UUID字符串，充当订单号，微信支付时候会用上
            form_4.setUuid(IdUtil.simpleUUID());     // UUID是自己生成的
            form_4.setCustomerId(customerId);
            form_4.setStartPlace(startPlace);
            form_4.setStartPlaceLatitude(startPlaceLatitude);
            form_4.setStartPlaceLongitude(startPlaceLongitude);
            form_4.setEndPlace(endPlace);
            form_4.setEndPlaceLatitude(endPlaceLatitude);
            form_4.setEndPlaceLongitude(endPlaceLongitude);
            form_4.setExpectsMileage(mileage);
            form_4.setExpectsFee(expectsFee);
            form_4.setFavourFee(favourFee);
            form_4.setDate(new DateTime().toDateStr());
            form_4.setChargeRuleId(Long.parseLong(chargeRuleId));
            form_4.setCarPlate(form.getCarPlate());
            form_4.setCarType(form.getCarType());
            form_4.setBaseMileage(baseMileage);
            form_4.setBaseMileagePrice(baseMileagePrice);
            form_4.setExceedMileagePrice(exceedMileagePrice);
            form_4.setBaseMinute(baseMinute);
            form_4.setExceedMinutePrice(exceedMinutePrice);
            form_4.setBaseReturnMileage(baseReturnMileage);
            form_4.setExceedReturnPrice(exceedReturnPrice);

            r = this.odrServiceApi.insertOrder(form_4);   // 插入订单
            String orderId = MapUtil.getStr(r, "result");


            /*
             * 发送新订单通知给相关司机
             */
            SendNewOrderMessageForm form_5 = new SendNewOrderMessageForm();
            String[] driverContent = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                HashMap one = list.get(i);
                String driverId = MapUtil.getStr(one, "driverId");
                String distance = MapUtil.getStr(one, "distance");
                distance = new BigDecimal(distance).setScale(1, RoundingMode.CEILING).toString();
                driverContent[i] = driverId + "#" + distance;
            }
            form_5.setDriversContent(driverContent);
            form_5.setOrderId(Long.parseLong(orderId));
            form_5.setFrom(startPlace);
            form_5.setTo(endPlace);
            form_5.setExpectsFee(expectsFee);
            //里程转化成保留小数点后一位
            mileage = new BigDecimal(mileage).setScale(1, RoundingMode.CEILING).toString();
            form_5.setMileage(mileage);
            form_5.setMinute(minute);
            form_5.setFavourFee(favourFee);
            this.snmServiceApi.sendNewOrderMessageAsync(form_5); //异步发送消息
            result.put("orderId", orderId);
            result.replace("count", list.size());
            result.put("orderId", orderId);
            result.replace("count", list.size());
        }
        return result;
    }


    @Override
    public Integer searchOrderStatus(SearchOrderStatusForm form) {
        R r = this.odrServiceApi.searchOrderStatus(form);
        Integer status = MapUtil.getInt(r, "result");
        return status;
    }

    @Override
    @Transactional
    @LcnTransaction
    public String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form) {
        R r = this.odrServiceApi.deleteUnAcceptOrder(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }

    @Override
    public HashMap hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form) {
        R r = this.odrServiceApi.hasCustomerCurrentOrder(form);
        HashMap map = (HashMap) r.get("result");
        return map;
    }

    @Override
    public boolean confirmArriveStartPlace(ConfirmArriveStartPlaceForm form) {
        R r = this.odrServiceApi.confirmArriveStartPlace(form);
        boolean result = MapUtil.getBool(r, "result");
        return result;
    }


    @Override
    public HashMap searchOrderForMoveById(SearchOrderForMoveByIdForm form) {
        R r = this.odrServiceApi.searchOrderForMoveById(form);
        HashMap map = (HashMap) r.get("result");
        return map;
    }

    @Override
    public HashMap searchOrderById(SearchOrderByIdForm form) {
        R r = this.odrServiceApi.searchOrderById(form);
        HashMap map = (HashMap) r.get("result");
        Long driverId = MapUtil.getLong(map, "driverId");
        if (driverId != null) {
            SearchDriverBriefInfoForm infoForm = new SearchDriverBriefInfoForm();
            infoForm.setDriverId(driverId);
            r = this.drServiceApi.searchDriverBriefInfo(infoForm);

            HashMap temp = (HashMap) r.get("result");
            map.putAll(temp);
            int status = MapUtil.getInt(map, "status");
            HashMap cmtMap = new HashMap();
            if (status >= 7) {
                SearchCommentByOrderIdForm commentForm = new SearchCommentByOrderIdForm();
                commentForm.setOrderId(form.getOrderId());
                commentForm.setCustomerId(form.getCustomerId());
                r = this.odrServiceApi.searchCommentByOrderId(commentForm);
                if (r.containsKey("result")) {
                    cmtMap = (HashMap) r.get("result");
                } else {
                    cmtMap.put("rate", 5);
                }
            }
            map.put("comment", cmtMap);
            return map;
        }

        return null;
    }

    @Override
    @Transactional
    @LcnTransaction
    public HashMap createWxPayment(long orderId, long customerId, Long voucherId) {
        /*
         * 1.先查询订单是否为6状态，其他状态都不可以生成支付订单
         */
        ValidCanPayOrderForm form_1 = new ValidCanPayOrderForm();
        form_1.setOrderId(orderId);
        form_1.setCustomerId(customerId);
        R r = this.odrServiceApi.validCanPayOrder(form_1);
        HashMap map = (HashMap) r.get("result");
        String amount = MapUtil.getStr(map, "realFee");
        String uuid = MapUtil.getStr(map, "uuid");
        long driverId = MapUtil.getLong(map, "driverId");
        String discount = "0.00";
        if (voucherId != null) {
            /*
             * 2.查询代金券是否可以使用，并绑定
             */
            UseVoucherForm form_2 = new UseVoucherForm();
            form_2.setCustomerId(customerId);
            form_2.setVoucherId(voucherId);
            form_2.setOrderId(orderId);
            form_2.setAmount(amount);
            r = this.vhrServiceApi.useVoucher(form_2);
            discount = MapUtil.getStr(r, "result");
        }
        if (new BigDecimal(amount).compareTo(new BigDecimal(discount)) == -1) {
            throw new HxdsException("总金额不能小于优惠劵面额");
        }
        /*
         * 3.修改实付金额
         */
        amount = NumberUtil.sub(amount, discount).toString();
        UpdateBillPaymentForm form_3 = new UpdateBillPaymentForm();
        form_3.setOrderId(orderId);
        form_3.setRealPay(amount);
        form_3.setVoucherFee(discount);
        this.odrServiceApi.updateBillPayment(form_3);

        /*
         * 4.查询用户的OpenID字符串
         */
        SearchCustomerOpenIdForm form_4 = new SearchCustomerOpenIdForm();
        form_4.setCustomerId(customerId);
        r = this.cstServiceApi.searchCustomerOpenId(form_4);
        String customerOpenId = MapUtil.getStr(r, "result");

        /*
         * 5.查询司机的OpenId字符串
         */
        SearchDriverOpenIdForm form_5 = new SearchDriverOpenIdForm();
        form_5.setDriverId(driverId);
        r = this.drServiceApi.searchDriverOpenId(form_5);
        String driverOpenId = MapUtil.getStr(r, "result");

        /*
         * 6. 创建支付订单
         */
        try {
            WXPay wxPay = new WXPay(this.myWXPayConfig);
            HashMap param = new HashMap();
            param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            param.put("body", "代驾费");
            param.put("out_trade_no", uuid);
            //充值金额转换成分为单位，并且让BigDecimal取整数
            //amount="1.00";
            param.put("total_fee", NumberUtil.mul(amount, "100").setScale(0, RoundingMode.FLOOR).toString());
            param.put("spbill_create_ip", "127.0.0.1");

            param.put("notify_url", "http://demo.com");   // TODO 这里要修改成内网穿透的公网URL
            param.put("trade_type", "JSAPI");
            param.put("openid", customerOpenId);
            param.put("attach", driverOpenId);
            param.put("profit_sharing", "Y"); //支付需要分账

            //创建支付订单
            Map<String, String> result = wxPay.unifiedOrder(param);

            //预支付交易会话标识ID
            String prepayId = result.get("prepay_id");
            if (prepayId != null) {
                /*
                 * 7.更新订单记录中的prepay_id字段值
                 */
                UpdateOrderPrepayIdForm form_6 = new UpdateOrderPrepayIdForm();
                form_6.setOrderId(orderId);
                form_6.setPrepayId(prepayId);
                this.odrServiceApi.updateOrderPrepayId(form_6);

                //准备生成数字签名用的数据
                map.clear();
                map.put("appId", this.myWXPayConfig.getAppID());
                String timeStamp = new Date().getTime() + "";
                map.put("timeStamp", timeStamp);
                String nonceStr = WXPayUtil.generateNonceStr();
                map.put("nonceStr", nonceStr);
                map.put("package", "prepay_id=" + prepayId);
                map.put("signType", "MD5");

                //生成数据签名
                String paySign = WXPayUtil.generateSignature(map, this.myWXPayConfig.getKey()); //生成数字签名

                map.clear(); //清理HashMap，放入结果
                map.put("package", "prepay_id=" + prepayId);
                map.put("timeStamp", timeStamp);
                map.put("nonceStr", nonceStr);
                map.put("paySign", paySign);
                //uuid用于付款成功后，移动端主动请求更新充值状态
                map.put("uuid", uuid);
                return map;
            } else {
                log.error("创建支付订单失败");
                throw new HxdsException("创建支付订单失败");
            }
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            throw new HxdsException("创建支付订单失败");
        }
    }



    @Override
    @Transactional
    @LcnTransaction
    public String updateOrderAboutPayment(UpdateOrderAboutPaymentForm form) {
        R r = this.odrServiceApi.updateOrderAboutPayment(form);
        String result = MapUtil.getStr(r, "result");
        return result;
    }

    @Override
    public PageUtils searchCustomerOrderByPage(SearchCustomerOrderByPageForm form) {
        R r = this.odrServiceApi.searchCustomerOrderByPage(form);
        HashMap map = (HashMap) r.get("result");
        PageUtils pageUtils = BeanUtil.toBean(map, PageUtils.class);
        return pageUtils;
    }
}

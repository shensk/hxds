package com.aomsir.hxds.odr.config;

import com.aomsir.hxds.odr.db.dao.OrderBillDao;
import com.aomsir.hxds.odr.db.dao.OrderDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Slf4j
@Component
public class KeyExpiredListener extends KeyExpirationEventMessageListener {
    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderBillDao orderBillDao;

    public KeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        //从消息队列中接收消息
        if (new String(message.getChannel()).equals("__keyevent@5__:expired")) {
            
            //反序列化Key，否则出现乱码
            JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();
            String key = serializer.deserialize(message.getBody()).toString();
            
            if (key.contains("order#")) {
                long orderId = Long.parseLong(key.split("#")[1]);
                HashMap param = new HashMap() {{
                    put("orderId", orderId);
                }};
                int rows = this.orderDao.deleteUnAcceptOrder(param);
                if (rows == 1) {
                    log.info("删除了无人接单的订单：" + orderId);
                }
                rows = this.orderBillDao.deleteUnAcceptOrderBill(orderId);
                if (rows == 1) {
                    log.info("删除了无人接单的账单：" + orderId);
                }
            }
        }
        super.onMessage(message, pattern);
    }
}

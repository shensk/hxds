package com.aomsir.hxds.snm.task;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.snm.entity.NewOrderMessage;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NewOrderMassageTask {
    @Resource
    private ConnectionFactory factory;

    /**
     * 同步发送新订单消息
     */
    public void sendNewOrderMessage(ArrayList<NewOrderMessage> list) {
        int ttl = 1 * 60 * 1000; //新订单消息缓存过期时间1分钟
        String exchangeName = "new_order_private"; //交换机的名字
        try (
                Connection connection = this.factory.newConnection();
                Channel channel = connection.createChannel();
        ) {
            //定义交换机，根据routing key路由消息
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
            HashMap param = new HashMap();
            for (NewOrderMessage message : list) {
                //MQ消息的属性信息
                HashMap map = new HashMap();
                map.put("orderId", message.getOrderId());
                map.put("from", message.getFrom());
                map.put("to", message.getTo());
                map.put("expectsFee", message.getExpectsFee());
                map.put("mileage", message.getMileage());
                map.put("minute", message.getMinute());
                map.put("distance", message.getDistance());
                map.put("favourFee", message.getFavourFee());
                //创建消息属性对象
                AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().contentEncoding("UTF-8")
                        .headers(map).expiration(ttl + "").build();

                String queueName = "queue_" + message.getUserId(); //队列名字
                String routingKey = message.getUserId(); //routing key
                //声明队列（持久化缓存消息，消息接收不加锁，消息全部接收完并不删除队列）
                channel.queueDeclare(queueName, true, false, false, param);    // 持久化
                channel.queueBind(queueName,exchangeName,routingKey);
                //向交换机发送消息，并附带routing key
                channel.basicPublish(exchangeName, routingKey, properties, ("新订单" + message.getOrderId()).getBytes());
                log.debug(message.getUserId() + "的新订单消息发送成功");
            }

        } catch (Exception e) {
            log.error("执行异常", e);
            throw new HxdsException("新订单消息发送失败");
        }
    }

    /**
     * 异步发送新订单消息
     */
    @Async
    public void sendNewOrderMessageAsync(ArrayList<NewOrderMessage> list) {
        sendNewOrderMessage(list);
    }


    /**
     * 同步接收新订单消息
     */
    public List<NewOrderMessage> receiveNewOrderMessage(long userId) {
        String exchangeName = "new_order_private"; //交换机名字
        String queueName = "queue_" + userId; //队列名字
        String routingKey = userId + ""; //routing key

        List<NewOrderMessage> list = new ArrayList();
        try (Connection connection = this.factory.newConnection();
             Channel privateChannel = connection.createChannel();
        ) {
            //定义交换机，routing key模式
            privateChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
            //声明队列（持久化缓存消息，消息接收不加锁，消息全部接收完并不删除队列）
            privateChannel.queueDeclare(queueName, true, false, false, null);
            //绑定要接收的队列
            privateChannel.queueBind(queueName, exchangeName, routingKey);
            //为了避免一次性接收太多消息，我们采用限流的方式，每次接收10条消息，然后循环接收
            privateChannel.basicQos(0, 10, true);

            while (true) {
                //从队列中接收消息
                GetResponse response = privateChannel.basicGet(queueName, false);
                if (response != null) {
                    //消息属性对象
                    AMQP.BasicProperties properties = response.getProps();
                    Map<String, Object> map = properties.getHeaders();
                    String orderId = MapUtil.getStr(map, "orderId");
                    String from = MapUtil.getStr(map, "from");
                    String to = MapUtil.getStr(map, "to");
                    String expectsFee = MapUtil.getStr(map, "expectsFee");
                    String mileage = MapUtil.getStr(map, "mileage");
                    String minute = MapUtil.getStr(map, "minute");
                    String distance = MapUtil.getStr(map, "distance");
                    String favourFee = MapUtil.getStr(map, "favourFee");

                    //把新订单的消息封装到对象中
                    NewOrderMessage message = new NewOrderMessage();
                    message.setOrderId(orderId);
                    message.setFrom(from);
                    message.setTo(to);
                    message.setExpectsFee(expectsFee);
                    message.setMileage(mileage);
                    message.setMinute(minute);
                    message.setDistance(distance);
                    message.setFavourFee(favourFee);

                    list.add(message);

                    byte[] body = response.getBody();
                    String msg = new String(body);
                    log.debug("从RabbitMQ接收的订单消息：" + msg);

                    //确认收到消息，让MQ删除该消息
                    long deliveryTag = response.getEnvelope().getDeliveryTag();
                    privateChannel.basicAck(deliveryTag, false);
                } else {
                    break;
                }
            }
            ListUtil.reverse(list); //消息倒叙，新消息排在前面
            return list;
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new HxdsException("接收新订单失败");
        }
    }
}

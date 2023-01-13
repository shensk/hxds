package com.aomsir.hxds.odr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import javax.annotation.Resource;

@Configuration
public class RedisConfiguration {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public ChannelTopic expiredTopic() {
        /*
         * 自定义Redis队列的名字，如果有缓存销毁，就自动往这个队列中发消息
         * 每个子系统有各自的Redis逻辑库，订单子系统不会监听到其他子系统缓存数据销毁
         */
        return new ChannelTopic("__keyevent@5__:expired");  
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(this.redisConnectionFactory);
        return redisMessageListenerContainer;
    }
}

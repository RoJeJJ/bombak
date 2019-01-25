package com.roje.bombak.room.api;

import com.roje.bombak.common.dispatcher.Dispatcher;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.room.api.config.RoomProperties;
import com.roje.bombak.room.api.executor.RoomCreateExecutorGroup;
import com.roje.bombak.room.api.executor.UserExecutorGroup;
import com.roje.bombak.room.api.manager.RoomIdGenerator;
import com.roje.bombak.room.api.manager.impl.RoomIdGeneratorImpl;
import com.roje.bombak.room.api.processor.RoomProcessor;
import com.roje.bombak.room.api.redis.RoomRedisDao;
import com.roje.bombak.room.api.redis.RoomRedisDaoImpl;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
@EnableConfigurationProperties(value = {RoomProperties.class})
@Configuration
public class RoomAutoConfiguration {
    @Bean
    public RoomRedisDao roomRedisDao(RedisTemplate<Object,Object> redisTemplate){
        return new RoomRedisDaoImpl(redisTemplate);
    }

    @Bean
    public UserExecutorGroup userExecutorGroup(RoomProperties properties){
        return new UserExecutorGroup(properties.getUserExecutorSize());
    }

    @Bean
    public RoomCreateExecutorGroup roomCreateExecutorGroup() {
        return new RoomCreateExecutorGroup();
    }

    @Bean
    public RoomIdGenerator roomIdGenerator(RedisTemplate<Object,Object> redisTemplate) {
        return new RoomIdGeneratorImpl(redisTemplate);
    }

    @Bean
    public RoomMessageSender roomMessageSender(ServiceInfo serviceInfo, AmqpTemplate amqpTemplate) {
        return new RoomMessageSender(serviceInfo,amqpTemplate);
    }

    @Bean
    public Dispatcher<RoomProcessor> roomDispatcher() {
        return new Dispatcher<>();
    }

}

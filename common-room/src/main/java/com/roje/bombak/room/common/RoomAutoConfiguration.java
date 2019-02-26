package com.roje.bombak.room.common;

import com.netflix.appinfo.ApplicationInfoManager;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.executor.JoinRoomExecutorGroup;
import com.roje.bombak.room.common.executor.RoomCreateExecutor;
import com.roje.bombak.room.common.manager.RoomIdGenerator;
import com.roje.bombak.room.common.manager.impl.RoomIdGeneratorImpl;
import com.roje.bombak.room.common.rabbit.RoomInstanceService;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.room.common.redis.RoomRedisDaoImpl;
import com.roje.bombak.room.common.utils.RoomMessageSender;
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
    public RoomIdGenerator roomIdGenerator(RedisTemplate<Object,Object> redisTemplate) {
        return new RoomIdGeneratorImpl(redisTemplate);
    }

    @Bean
    public RoomMessageSender roomMessageSender(ServiceInfo serviceInfo, AmqpTemplate amqpTemplate) {
        return new RoomMessageSender(serviceInfo,amqpTemplate);
    }

    @Bean
    public RoomCreateExecutor roomCreateExecutor() {
        return new RoomCreateExecutor();
    }

    @Bean
    public RoomInstanceService roomInstanceService(ApplicationInfoManager manager) {
        return new RoomInstanceService(manager);
    }
}

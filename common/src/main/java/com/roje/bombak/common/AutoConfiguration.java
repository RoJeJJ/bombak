package com.roje.bombak.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.roje.bombak.common.config.properties.RedissonProperties;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.common.redis.dao.impl.UserRedisDaoImpl;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.common.processor.RecForwardClientMessageProcessor;
import com.roje.bombak.common.processor.Dispatcher;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

/**
 * @author pc
 */
@Configuration
@EnableConfigurationProperties(value = {
        RedissonProperties.class
})
public class AutoConfiguration {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        redisTemplate.setDefaultSerializer(jackson2JsonRedisSerializer);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @ConditionalOnBean(RedisTemplate.class)
    @Bean
    public UserRedisDao userRedisService(RedisTemplate<Object, Object> redisTemplate) {
        return new UserRedisDaoImpl(redisTemplate);
    }


    /**
     * 单机模式自动装配
     */
    @Bean
    @ConditionalOnProperty(name="redisson.address")
    public RedissonClient redissonSingle(RedissonProperties redissonProperties) {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(redissonProperties.getAddress())
                .setTimeout(redissonProperties.getTimeout())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());

        if(StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }

        return Redisson.create(config);
    }

    /**
     * 哨兵模式自动装配
     */
    @Bean
    @ConditionalOnProperty(name="redisson.master-name")
    public RedissonClient redissonSentinel(RedissonProperties redissonProperties) {
        Config config = new Config();
        SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(redissonProperties.getSentinelAddresses())
                .setMasterName(redissonProperties.getMasterName())
                .setTimeout(redissonProperties.getTimeout())
                .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize());

        if(StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(name = "eureka.instance.ip-address")
    public ServiceInfo serviceInfo(ApplicationInfoManager infoManager) {
        ServiceInfo info = new ServiceInfo();
        InstanceInfo instanceInfo = infoManager.getInfo();
        info.setServiceType(instanceInfo.getAppName().toLowerCase());
        info.setIp(instanceInfo.getIPAddr());
        info.setPort(instanceInfo.getPort());
        Map<String,String> meta = instanceInfo.getMetadata();
        info.setServiceId(meta.get("id"));
        return info;
    }

    @ConditionalOnMissingBean(MessageSender.class)
    @Bean
    public MessageSender roomMessageSender(ServiceInfo serviceInfo,AmqpTemplate amqpTemplate) {
        return new MessageSender(serviceInfo,amqpTemplate);
    }

    @Bean
    public Dispatcher dispatcher(){
        return new Dispatcher();
    }
}

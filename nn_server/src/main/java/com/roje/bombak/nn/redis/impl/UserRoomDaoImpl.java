package com.roje.bombak.nn.redis.impl;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.nn.redis.UserRoomDao;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
@SuppressWarnings("unchecked")
@Component
public class UserRoomDaoImpl implements UserRoomDao {

    private static final String USER_ROOM_REDIS = "user_room";

    private final RedisTemplate redisTemplate;

    public UserRoomDaoImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public ServiceInfo getUserRoomService(long uid) {
        return (ServiceInfo) redisTemplate.opsForHash().get(USER_ROOM_REDIS,uid);
    }

    @Override
    public boolean setUserRoomService(long uid, ServiceInfo roomServiceInfo) {
        return redisTemplate.opsForHash().putIfAbsent(USER_ROOM_REDIS,uid,roomServiceInfo);
    }

    @Override
    public void removeUserRoomService(long uid) {
        redisTemplate.opsForHash().delete(USER_ROOM_REDIS,uid);
    }
}

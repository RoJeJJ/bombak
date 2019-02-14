package com.roje.bombak.room.api.redis;

import com.roje.bombak.common.api.eureka.ServiceInfo;
import com.roje.bombak.room.api.constant.Constant;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public class RoomRedisDaoImpl implements RoomRedisDao {

    private final RedisTemplate<Object,Object> redisTemplate;

    public RoomRedisDaoImpl(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public boolean setUserRoomService(long uid, ServiceInfo serviceInfo) {
        return redisTemplate.opsForHash().putIfAbsent(Constant.RedisConstant.USER_ROOM,uid,serviceInfo);
    }

    @Override
    public ServiceInfo getUserRoomService(long uid) {
        return (ServiceInfo) redisTemplate.opsForHash().get(Constant.RedisConstant.USER_ROOM,uid);
    }

    @Override
    public void removeUserRoomService(long uid) {
        redisTemplate.opsForHash().delete(Constant.RedisConstant.USER_ROOM,uid);
    }

    @Override
    public void setRoomService(long roomId, ServiceInfo serviceInfo) {
        redisTemplate.boundHashOps(Constant.RedisConstant.ROOM).put(roomId,serviceInfo);
    }

    @Override
    public ServiceInfo getRoomService(long roomId) {
        return (ServiceInfo) redisTemplate.boundHashOps(Constant.RedisConstant.ROOM).get(roomId);
    }

    @Override
    public void removeRoomService(long roomId) {
        redisTemplate.opsForHash().delete(Constant.RedisConstant.ROOM,roomId);
    }

    @Override
    public void setGoldRoomNo() {
        redisTemplate.opsForValue().setIfAbsent(Constant.RedisConstant.ROOM_NO_REDIS,1);
    }

    @Override
    public Long getGoldRoomNo() {
        return redisTemplate.opsForValue().increment(Constant.RedisConstant.ROOM_NO_REDIS);
    }
}

package com.roje.bombak.room.common.redis;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.room.common.constant.RoomConstant;
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
        return redisTemplate.opsForHash().putIfAbsent(RoomConstant.RedisConstant.USER_ROOM,uid,serviceInfo);
    }

    @Override
    public ServiceInfo getUserRoomService(long uid) {
        return (ServiceInfo) redisTemplate.opsForHash().get(RoomConstant.RedisConstant.USER_ROOM,uid);
    }

    @Override
    public void removeUserRoomService(long uid) {
        redisTemplate.opsForHash().delete(RoomConstant.RedisConstant.USER_ROOM,uid);
    }

    @Override
    public void setRoomService(long roomId, ServiceInfo serviceInfo) {
        redisTemplate.boundHashOps(RoomConstant.RedisConstant.ROOM).put(roomId,serviceInfo);
    }

    @Override
    public ServiceInfo getRoomService(long roomId) {
        return (ServiceInfo) redisTemplate.boundHashOps(RoomConstant.RedisConstant.ROOM).get(roomId);
    }

    @Override
    public void removeRoomService(long roomId) {
        redisTemplate.opsForHash().delete(RoomConstant.RedisConstant.ROOM,roomId);
    }

    @Override
    public void setGoldRoomNo() {
        redisTemplate.opsForValue().setIfAbsent(RoomConstant.RedisConstant.ROOM_NO_REDIS,1);
    }

    @Override
    public Long getGoldRoomNo() {
        return redisTemplate.opsForValue().increment(RoomConstant.RedisConstant.ROOM_NO_REDIS);
    }
}

package com.roje.bombak.room.api.manager.impl;

import com.roje.bombak.room.api.manager.RoomIdGenerator;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Random;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public class RoomIdGeneratorImpl implements RoomIdGenerator {

    private final RedisTemplate<Object,Object> redisTemplate;

    private final Random random = new Random();

    private final static String ROOM_ID = "room_id";

    public RoomIdGeneratorImpl(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long getId() {
        Long i;
        long id;
        do {
            id = rand();
            i = redisTemplate.opsForSet().add(ROOM_ID,id);
        } while (i == null || i == 0);

        return id;
    }

    private long rand() {
     return 100000 + random.nextInt(900000);
    }

    @Override
    public void removeId(long id) {
        redisTemplate.opsForSet().remove(ROOM_ID,id);
    }
}

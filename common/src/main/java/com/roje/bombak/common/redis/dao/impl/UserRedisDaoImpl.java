package com.roje.bombak.common.redis.dao.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.model.User;
import com.roje.bombak.common.model.impl.SimpleUser;
import com.roje.bombak.common.redis.constant.RedisConstant;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author pc
 */
@Slf4j
public class UserRedisDaoImpl implements UserRedisDao, RedisConstant {

    private static final long INITIAL_ID = 100000;

    private final RedisTemplate<Object, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DefaultRedisScript<Boolean> redisScript;

    private static final String SCRIPT_SUB_GOLD_IF_ENOUGH = "local gold = redis.call('hget',KEYS[1],'gold')\n" +
            "gold = tonumber(gold)\n" +
            "local subGold = tonumber(ARGV[1])\n" +
            "\n" +
            "if(gold >= subGold) then\n" +
            "    redis.call('set',KEYS[1],gold - sub)\n" +
            "    return true\n" +
            "else\n" +
            "    return false\n" +
            "end";

    public UserRedisDaoImpl(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisScript = new DefaultRedisScript<>(SCRIPT_SUB_GOLD_IF_ENOUGH);
        redisScript.setResultType(Boolean.class);
    }


    @Override
    public void setUser(User user) {
        try {
            String userJson = objectMapper.writeValueAsString(user);
            Map userMap = objectMapper.readValue(userJson, Map.class);
            redisTemplate.opsForHash().putAll(USER + user.id(), userMap);
        } catch (IOException e) {
            log.error("保存user到redis出现异常", e);
        }
    }

    @Override
    public User getUser(Long id) {
        if (id == null) {
            return null;
        }
        Map<Object, Object> map = redisTemplate.opsForHash().entries(USER + id);
        if (map.size() == 0) {
            return null;
        }
        try {
            String userJson = objectMapper.writeValueAsString(map);
            return objectMapper.readValue(userJson, SimpleUser.class);
        } catch (IOException e) {
            log.error("在redis中取user出现异常", e);
            return null;
        }
    }

    @Override
    public Long getAccountId(String account) {
        Number id = (Number) redisTemplate.opsForHash().get(ACCOUNT_ID, account);
        if (id == null) {
            return null;
        }
        return id.longValue();
    }

    @Override
    public void setAccountId(User user) {
        redisTemplate.opsForHash().put(ACCOUNT_ID, user.account(), user.id());
    }

    @Override
    public void setToken(User user, String token) {
        redisTemplate.opsForValue().set(user.id() + USER_TOKEN_SUFFIX, token, 5, TimeUnit.MINUTES);
    }

    @Override
    public String getToken(Long id) {
        return (String) redisTemplate.opsForValue().get(id + USER_TOKEN_SUFFIX);
    }

    @Override
    public void setGateInfo(long uid, ServiceInfo info) {
        redisTemplate.opsForHash().put(USER_GATE, uid, info);
    }

    @Override
    public ServiceInfo getGateInfo(long uid) {
        return (ServiceInfo) redisTemplate.opsForHash().get(USER_GATE, uid);
    }

    @Override
    public void removeGateInfo(long uid) {
        redisTemplate.opsForHash().delete(USER_GATE, uid);
    }

    @Override
    public void setInitialId() {
        redisTemplate.opsForValue().setIfAbsent(USER_ID,INITIAL_ID);
    }

    @Override
    public Long getUserId() {
        return redisTemplate.opsForValue().increment(USER_ID);
    }

    @Override
    public boolean minusGoldIfEnough(long uid, int gold) {
        List<Object> keys = new ArrayList<>();
        keys.add(USER + uid);
        Boolean b =  redisTemplate.execute(redisScript,keys,gold);
        if (b == null) {
            return false;
        } else {
            return b;
        }
    }

}

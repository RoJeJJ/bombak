package com.roje.bombak.login.service.impl;

import com.roje.bombak.common.model.User;
import com.roje.bombak.common.model.impl.SimpleUser;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.login.response.ResponseData;
import com.roje.bombak.login.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * @author pc
 */
@Slf4j
@Component
public class UserServiceImpl implements UserService<ResponseData> {

    private static final int MIN_PASSWORD_LEN = 6;

    private final UserRedisDao userRedisDao;

    private final LoadBalancerClient loadBalancerClient;


    public UserServiceImpl(UserRedisDao userRedisDao,
                           LoadBalancerClient loadBalancerClient) {
        this.userRedisDao = userRedisDao;
        this.loadBalancerClient = loadBalancerClient;
        userRedisDao.setInitialId();
    }

    @Override
    public ResponseData login(String account, String password) {
        if (StringUtils.isBlank(account)) {
            return ResponseData.account_is_blank;
        }
        if (StringUtils.isBlank(password)) {
            return ResponseData.password_is_blank;
        }
        Long uid;
        User user;
        synchronized (account.intern()) {
            uid = userRedisDao.getAccountId(account);
            user = userRedisDao.getUser(uid);
            if (user == null || !user.getPassword().equals(password)) {
                log.info("用户名或密码错误");
                return ResponseData.account_or_password_error;
            }
        }
        //禁止登录
        if (user.getState() != 0) {
            log.info("用户:{}被禁止登录", account);
            return ResponseData.login_banned;
        }
        ServiceInstance instance = loadBalancerClient.choose("gate");
        if (instance == null) {
            log.warn("没有可用网关服务器");
            return ResponseData.gate_not_found;
        }

        String ip = instance.getHost();
        int port = Integer.parseInt(instance.getMetadata().get("port"));
        String token = UUID.randomUUID().toString().replace("-", "");
        userRedisDao.setToken(user, token);

        ResponseData resp = ResponseData.success;
        resp.setUid(uid);
        resp.setToken(token);
        resp.setIp(ip);
        resp.setPort(port);
        return ResponseData.success;
    }

    @Override
    public ResponseData loginWeChat(String openId, String token) {
        return null;
    }

    @Override
    public ResponseData register(String account, String password) {
        if (StringUtils.isBlank(account)) {
            return ResponseData.account_is_blank;
        }
        if (StringUtils.isBlank(password)) {
            return ResponseData.password_is_blank;
        }
        if (password.length() < MIN_PASSWORD_LEN) {
            log.info("密码长度不够");
            return ResponseData.register_password_len_not_enough;
        }
        synchronized (account.intern()) {
            Long id = userRedisDao.getAccountId(account);
            if (id != null) {
                return ResponseData.register_account_exists;
            }
            Long newId = userRedisDao.getUserId();
            User user = new SimpleUser(newId, account,new Date());
            user.setPassword(password);
            user.setNickname(account);
            user.setHeadImg("");
            user.setRoomCard(12);
            user.setGold(10000);
            user.setVip(0);
            user.setState(0);

            redisSave(user);

            return ResponseData.success;
        }
    }

    private void redisSave(User user) {
        userRedisDao.setUser(user);
        userRedisDao.setAccountId(user);
    }

}

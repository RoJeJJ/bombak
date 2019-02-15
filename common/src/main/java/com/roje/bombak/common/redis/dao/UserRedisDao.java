package com.roje.bombak.common.redis.dao;


import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.model.User;


/**
 * @author pc
 */
public interface UserRedisDao {

    /**
     * 保存user
     * @param user 用户
     */
    void setUser(User user);

    /**
     * 根据id获取用户
     * @param id id
     * @return 用户
     */
    User getUser(Long id);

    /**
     * 将账号和Id绑定
     * @param user 用户
     */
    void setAccountId(User user);

    /**
     * 通过账号获取id
     * @param account 账号
     * @return id
     */
    Long getAccountId(String account);

    /**
     * 设置token
     * @param user 用户
     * @param token token
     */
    void setToken(User user,String token);

    /**
     * 根据id获取token
     * @param uid 用户id
     * @return token
     */
    String getToken(Long uid);

    /**
     * 绑定用户连接的网关
     * @param uid 用户id
     * @param info 网关配置
     */
    void setGateInfo(long uid, ServiceInfo info);

    /**
     * 获取获取服务器信息
     * @param uid 用户id
     * @return 网关配置
     */
    ServiceInfo getGateInfo(long uid);

    /**
     * 移除用户网关
     * @param uid uid
     */
    void removeGateInfo(long uid);

    void setInitialId();

    Long getUserId();
}

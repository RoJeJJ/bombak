package com.roje.bombak.login.service;

/**
 * @author pc
 */
public interface UserService<T> {
    /**
     * 账号密码登录
     * @param account 账号
     * @param password 密码
     * @return {@link com.roje.bombak.login.response.ResponseData}
     */
    T login(String account,String password);

    /**
     * 微信登录
     * @param openId openId
     * @param token token
     * @return {@link com.roje.bombak.login.response.ResponseData}
     */
    T loginWeChat(String openId,String token);

    /**
     * 注册账号
     * @param account 注册名
     * @param password 注册密码
     * @return {@link com.roje.bombak.login.response.ResponseData}
     */
    T register(String account,String password);
}

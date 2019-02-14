package com.roje.bombak.common.api.model;

import java.util.Date;

/**
 * @author pc
 */
public interface User  {

    /**
     * 用户id ,唯一
     * @return id
     */
    long id();

    /**
     * 账户名,唯一
     * @return 账户名
     */
    String account();

    /**
     * 用户名密码
     * @return 用户名密码
     */
    String getPassword();

    /**
     * 设置用户密码
     * @param password 新密码
     */
    void setPassword(String password);

    /**
     * 用户昵称
     * @return 昵称
     */
    String getNickname();

    /**
     * 设置用户昵称,昵称可以相同
     * @param nickname 新昵称
     */
    void setNickname(String nickname);

    /**
     * 用户头像
     * @return 用户头像
     */
    String getHeadImg();

    /**
     * 设置用户头像
     * @param headImg 新头像
     */
    void setHeadImg(String headImg);

    /**
     * 性别
     * @return 性别
     */
    int getSex();

    /**
     * 修改性别
     * @param sex 性别
     */
    void setSex(int sex);

    /**
     * 房卡
     * @return 房卡
     */
    long getRoomCard();

    /**
     * 修改房卡
     * @param card 当前房卡
     */
    void setRoomCard(long card);

    /**
     * 金币
     * @return 金币
     */
    long getGold();

    /**
     * 修改金币
     * @param gold 金币
     */
    void setGold(long gold);

    /**
     * vip等级 0:普通会员
     * @return vip等级 int
     */
    int getVip();

    /**
     * 修改用户vip等级
     * @param vip 等级
     */
    void setVip(int vip);

    /**
     * 注册时间
     * @return 注册时间
     */
    Date registerDate();

    /**
     * 最后登录时间
     * @return 最后登录时间
     */
    Date getLastLoginDate();

    /**
     * 设置最后登录时间
     * @param lastLoginDate 最后登录时间
     */
    void setLastLoginDate(Date lastLoginDate);

    /**
     * 最后登出时间
     * @return 最后登出时间
     */
    Date getLastLogoutDate();

    /**
     * 设置最后登出时间
     * @param lastLogoutDate 最后登出时间
     */
    void setLastLogoutDate(Date lastLogoutDate);

    /**
     * 用户状态 0:正常 1:禁止登录
     * @return int 状态
     */
    int getState();

    /**
     * 设置用户状态
     * @param state
     */
    void setState(int state);
}

package com.roje.bombak.common.api.model.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.roje.bombak.common.api.model.User;

import java.util.Date;

/**
 * @author pc
 */
public class SimpleUser implements User {

    private final long id;

    private final String account;

    private String password;

    private String nickname;

    private String headImg;

    private int sex;

    private long roomCard;

    private long gold;

    private int vip;

    private final Date registerDate;

    private Date lastLoginDate;

    private Date lastLogoutDate;

    private int state;

    @JsonCreator
    public SimpleUser(@JsonProperty("id") long id,
                      @JsonProperty("account") String account,
                      @JsonProperty("registerDate") Date registerDate) {
        this.id = id;
        this.account = account;
        this.registerDate = registerDate;
    }

    @Override
    @JsonGetter
    public long id() {
        return id;
    }

    @Override
    @JsonGetter
    public String account() {
        return account;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getHeadImg() {
        return headImg;
    }

    @Override
    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    @Override
    public int getSex() {
        return sex;
    }

    @Override
    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public long getRoomCard() {
        return roomCard;
    }

    @Override
    public void setRoomCard(long card) {
        this.roomCard = card;
    }

    @Override
    public long getGold() {
        return gold;
    }

    @Override
    public void setGold(long gold) {
        this.gold = gold;
    }

    @Override
    public int getVip() {
        return vip;
    }

    @Override
    public void setVip(int vip) {
        this.vip = vip;
    }

    @Override
    @JsonGetter
    public Date registerDate() {
        return registerDate;
    }

    @Override
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    @Override
    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @Override
    public Date getLastLogoutDate() {
        return lastLogoutDate;
    }

    @Override
    public void setLastLogoutDate(Date lastLogoutDate) {
        this.lastLogoutDate = lastLogoutDate;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "SimpleUser{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", headImg='" + headImg + '\'' +
                ", sex=" + sex +
                ", roomCard=" + roomCard +
                ", gold=" + gold +
                ", vip=" + vip +
                ", registerDate=" + registerDate +
                ", lastLoginDate=" + lastLoginDate +
                ", lastLogoutDate=" + lastLogoutDate +
                ", state=" + state +
                '}';
    }
}

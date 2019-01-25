package com.roje.bombak.nn.config;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
@ConfigurationProperties(prefix = "nn")
public class NnProperties {

    private int roomMaxGamer = 10;

    private int roomMinGamer = 5;

    private int[] baseScore = new int[]{1,2,5,10,20};

    private int multiBetLimit = 5;

    private int multiRushLimit = 10;

    private int betTime = 20;

    private int rushTime = 5;

    private int checkTime = 10;

    public Map<Integer, Integer> getRoundFee() {
        return roundFee;
    }

    public void setRoundFee(Map<Integer, Integer> roundFee) {
        this.roundFee = roundFee;
    }

    private Map<Integer,Integer> roundFee = new HashMap<>();

    public int getRoomMaxGamer() {
        return roomMaxGamer;
    }

    public void setRoomMaxGamer(int roomMaxGamer) {
        this.roomMaxGamer = roomMaxGamer;
    }

    public int getRoomMinGamer() {
        return roomMinGamer;
    }

    public void setRoomMinGamer(int roomMinGamer) {
        this.roomMinGamer = roomMinGamer;
    }

    public int[] getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(int[] baseScore) {
        this.baseScore = baseScore;
    }

    public int getMultiBetLimit() {
        return multiBetLimit;
    }

    public void setMultiBetLimit(int multiBetLimit) {
        this.multiBetLimit = multiBetLimit;
    }

    public int getMultiRushLimit() {
        return multiRushLimit;
    }

    public void setMultiRushLimit(int multiRushLimit) {
        this.multiRushLimit = multiRushLimit;
    }

    public int getBetTime() {
        return betTime;
    }

    public void setBetTime(int betTime) {
        this.betTime = betTime;
    }

    public int getRushTime() {
        return rushTime;
    }

    public void setRushTime(int rushTime) {
        this.rushTime = rushTime;
    }

    public int getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(int checkTime) {
        this.checkTime = checkTime;
    }
}

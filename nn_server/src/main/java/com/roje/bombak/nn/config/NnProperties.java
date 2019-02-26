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

    private int betSecondTime = 20;

    private int rushSecondTime = 5;

    private int checkSecondTime = 10;

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
        if (multiBetLimit > 0) {
            this.multiBetLimit = multiBetLimit;
        }
    }

    public int getMultiRushLimit() {
        return multiRushLimit;
    }

    public void setMultiRushLimit(int multiRushLimit) {
        if (multiRushLimit > 0) {
            this.multiRushLimit = multiRushLimit;
        }
    }

    public int getBetSecondTime() {
        return betSecondTime;
    }

    public void setBetSecondTime(int betTime) {
        if (betTime > 0) {
            this.betSecondTime = betTime;
        }
    }

    public int getRushSecondTime() {
        return rushSecondTime;
    }

    public void setRushSecondTime(int rushTime) {
        if (rushTime > 0) {
            this.rushSecondTime = rushTime;
        }
    }

    public int getCheckSecondTime() {
        return checkSecondTime;
    }

    public void setCheckSecondTime(int checkTime) {
        if (checkSecondTime > 0) {
            this.checkSecondTime = checkTime;
        }
    }
}

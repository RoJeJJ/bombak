package com.roje.bombak.room.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
@ConfigurationProperties(prefix = "room")
public class RoomProperties {

    private int userExecutorSize = 5;

    private int executorRoomSize = 4;

    private int maxRoomSize = 100;

    private int roomExecutorSize = 3;

    private int roomMaxPlayers = 100;

    private Map<Integer,Integer> roundCardMap = new HashMap<>();

    public int getExecutorRoomSize() {
        return executorRoomSize;
    }

    public void setExecutorRoomSize(int executorRoomSize) {
        if (executorRoomSize > 0) {
            this.executorRoomSize = executorRoomSize;
        }
    }

    public int getMaxRoomSize() {
        return maxRoomSize;
    }

    public void setMaxRoomSize(int maxRoomSize) {
        this.maxRoomSize = maxRoomSize;
    }

    public int getUserExecutorSize() {
        return userExecutorSize;
    }

    public void setUserExecutorSize(int userExecutorSize) {
        if (userExecutorSize > 0) {
            this.userExecutorSize = userExecutorSize;
        }
    }

    public int getRoomExecutorSize() {
        return roomExecutorSize;
    }

    public void setRoomExecutorSize(int roomExecutorSize) {
        if (roomExecutorSize > this.roomExecutorSize) {
            this.roomExecutorSize = roomExecutorSize;
        }
    }

    public int getRoomMaxPlayers() {
        return roomMaxPlayers;
    }

    public void setRoomMaxPlayers(int roomMaxPlayers) {
        this.roomMaxPlayers = roomMaxPlayers;
    }

    public Map<Integer, Integer> getRoundCardMap() {
        return roundCardMap;
    }

    public void setRoundCardMap(Map<Integer, Integer> roundCardMap) {
        this.roundCardMap = roundCardMap;
    }
}

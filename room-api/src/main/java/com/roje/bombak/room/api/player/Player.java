package com.roje.bombak.room.api.player;

import com.google.protobuf.Message;
import com.roje.bombak.room.api.proto.RoomMsg;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface Player {

    long uid();

    void setNickname(String nickname);

    String getNickname();

    void setHeadImg(String headImg);

    String getHeadImg();

    void setRoomCard(long roomCard);

    long getRoomCard();

    void setGold(long gold);

    long getGold();

    int seat();

    void sit(int seatNo);

    void setOffline(boolean bool);

    boolean isOffline();

    boolean isExit();

    void setExit(boolean exit);

    String getServiceType();

    void setServiceType(String type);

    String getServiceId();

    void setServiceId(String gateId);

    boolean isReady();

    void setReady(boolean ready);

    boolean isInGame();

    void setDisbandStatus(RoomMsg.DisbandStatus status);

    RoomMsg.DisbandStatus getDisbandStatus();

    Message playerDataToSelf();

    Message playerDataToOthers();
}

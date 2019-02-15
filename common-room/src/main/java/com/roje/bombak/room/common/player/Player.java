package com.roje.bombak.room.common.player;

import com.google.protobuf.Message;

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

    int getSeat();

    void setSeat(int seat);

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

    void setVoteStatus(VoteStatus status);

    VoteStatus getVoteStatus();

    /**
     * protobuf序列化的玩家
     * @param p 接收玩家数据的玩家
     * @return 玩家数据
     */
    Message playerData(Player p);
}

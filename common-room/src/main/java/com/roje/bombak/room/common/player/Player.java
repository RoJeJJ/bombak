package com.roje.bombak.room.common.player;

import com.google.protobuf.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.model.User;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface Player {

    long getUid();

    boolean isExit();

    void setExit(boolean exit);

    boolean isOffline();

    void setOffline(boolean offline);

    int getSeat();

    void setSeat(int seat);

    User getUser();

    void setUser(User user);

    String getSessionId();

    void setSessionId(String sessionId);

    ServiceInfo getGateInfo();

    void setGateInfo(ServiceInfo gateInfo);

    Message playerData(Player player);

    boolean isInGame();

    void setVoteStatus(VoteStatus wait);

    VoteStatus getVoteStatus();
}

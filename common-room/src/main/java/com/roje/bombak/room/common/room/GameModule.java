package com.roje.bombak.room.common.room;

import com.roje.bombak.room.common.player.Player;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/20
 **/
public interface GameModule<P extends Player,R extends Room<P>> {
    /**
     * 获取模块的房间
     * @return {@link Room}
     */
    R getRoom();

    void setRoom(R room);

    /**
     * 获取游戏模块的执行线程
     * @return {@link EventExecutor}
     */
    EventExecutor getExecutor();

    int getCapacity();

    /**
     * 重新进入房间
     * @param player 玩家
     * @param sessionId sessionId
     */
    void reJoin(P player,String sessionId);

    /**
     * 玩家上线
     * @param player 玩家
     * @param sessionId sessionId
     */
    void online(P player,String sessionId);

    /**
     * 玩家加入房间
     * @param uid 玩家id
     * @param sessionId sessionId
     */
    void join(long uid,String sessionId);
}

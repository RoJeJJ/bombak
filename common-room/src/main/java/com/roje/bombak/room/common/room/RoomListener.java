package com.roje.bombak.room.common.room;

import com.roje.bombak.room.common.player.Player;

/**
 * @author pc
 */
public interface RoomListener<P extends Player, R extends Room<P>> {
    /**
     * 房间关闭
     *
     * @param room 房间
     */
    void roomClosed(R room);

    /**
     * 玩家离开房间
     *
     * @param player 玩家
     * @param room 房间
     */
    void leaveRoom(P player, R room);
}
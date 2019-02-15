package com.roje.bombak.room.common.manager;

import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.room.Room;
import com.roje.bombak.room.common.room.RoomListener;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface RoomManager<P extends Player,R extends Room<P>> extends RoomListener<P,R> {
    /**
     * 创建房间
     * @param message 客户端消息
     */
    void createRoom(ServerMsg.ForwardClientMessage message);

    /**
     * 加入房间
     * @param message 客户端消息
     */
    void joinRoom(ServerMsg.ForwardClientMessage message) throws Exception;

    /**
     * 获取玩家所在房间
     * @param uid 玩家id
     * @return 返回所在房间,不在房间中返回null
     */
    R getPlayerRoom(long uid);
}

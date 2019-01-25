package com.roje.bombak.room.api.manager;

import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.room.Room;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface RoomManager<P extends Player,R extends Room<P>> {
    /**
     * 根据房间号获取房卡房间实例
     * @param roomId 房间号
     * @return 房卡房间实例
     */
    R getCardRoom(long roomId);

    /**
     * 根据房间号获取金币房间实例
     * @param roomId 房间号
     * @return 金币房间实例
     */
    R getGoldRoom(long roomId);

    /**
     * 根据用户id,获取用户所在房间
     * @param uid 用户id
     * @return 用户所在房间
     */
    R getPlayerRoom(long uid);
    /**
     * 移除房间内的玩家
     * @param room 移除玩家的房间
     * @param player 被移除的玩家
     */
    void removeRoomPlayer(R room, P player);

    /**
     * 移除房间
     * @param room 被移除的房间
     */
    void removeRoom(R room);

    /**
     * 获取房间最大人数
     * @return 人数
     */
    int getRoomMaxPlayer();

    /**
     * 游戏名称
     * @return 游戏名称
     */
    String getGameName();

    /**
     * 加入房间
     * @param message 客户端消息
     * @param room 加入的房间
     */
    void joinRoom(InnerClientMessage message, R room);

    /**
     * 创建房间
     * @param message 客户端消息
     * @param type 房间类型
     */
    void createRoom(InnerClientMessage message, RoomMsg.RoomType type);
}

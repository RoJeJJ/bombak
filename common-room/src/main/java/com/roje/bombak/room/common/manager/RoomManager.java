package com.roje.bombak.room.common.manager;

import com.google.protobuf.Any;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.model.User;
import com.roje.bombak.room.common.exception.CreateRoomException;
import com.roje.bombak.room.common.player.BasePlayer;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.room.Room;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface RoomManager<P extends Player,R extends Room<P>> {

    /**
     * 获取本服务器游戏房间数量
     * @return 房间总数量
     */
    int getRoomSize();

    /**
     * 创建房间
     * @param creatorId 创建人id
     * @param roomSetting 房间设置
     * @return 创建的房间
     * @throws CreateRoomException 创建失败抛出异常
     */
    R createRoom(long creatorId, Any roomSetting) throws CreateRoomException;
    /**
     * 获取玩家所在房间
     * @param uid 玩家id
     * @return 返回所在房间,不在房间中返回null
     */
    R getJoinRoom(long uid);

    /**
     * 根据房间号 获取房间
     * @param roomId 房间号
     * @return 指定房间,没有找到返回null
     */
    R getRoom(long roomId);

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
    void playerLeaveRoom(P player, R room);

    /**
     * 玩家加入了房间
     * @param player 玩家
     * @param room 房间
     */
    void playerJoinedRoom(P player,R room);

    /**
     * 根据id获取账户信息
     * @param uid 玩家id
     * @return {@link User}
     */
    User getUser(long uid);

    /**
     * 获取指定id的玩家的网关信息
     * @param uid id
     * @return {@link ServiceInfo}
     */
    ServiceInfo getUserGateInfo(long uid);

    void addRoom(R room);

    /**
     * 减去玩家金币,如果玩家金币足够,则减去,不足
     * @param player 玩家
     * @param gold <code>gold</code>
     * @return 足够减去 true,不足false
     */
    boolean minusGoldIfEnough(P player, int gold);
}

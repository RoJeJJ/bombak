package com.roje.bombak.room.api.room;

import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public interface Room<P extends Player> {

    /**
     * 房间是否关闭
     * @return boolean
     */
    boolean isClosed();
    /**
     * 房间id
     * @return id
     */
    long id();

    /**
     * 房间名称
     * @return 房间名字
     */
    String name();

    /**
     * 房间线程
     * @return 房间线程
     */
    EventExecutor executor();

    /**
     * 根据uid获取房间内玩家
     * @param uid 用户id
     * @return 玩家
     */
    P getPlayer(long uid);

    /**
     * 房间类型
     * @return {@link com.roje.bombak.room.api.proto.RoomMsg.RoomType}
     */
    RoomMsg.RoomType roomType();

    /**
     * 根据用户id生成新的玩家对象
     * @param uid 用户id
     * @return 玩家对象
     */
    P newPlayer(long uid);

    /**
     * 加入房间
     * @param message 客户端消息
     * @return 加入成功返回true,否则返回false
     */
    boolean join(InnerClientMessage message);

}

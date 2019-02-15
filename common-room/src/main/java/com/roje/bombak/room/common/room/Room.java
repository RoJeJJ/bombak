package com.roje.bombak.room.common.room;

import com.roje.bombak.room.common.player.Player;
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
     * @return {@link RoomType}
     */
    RoomType roomType();

//    /**
//     * 玩家是否已经在房间中
//     * @param player 玩家
//     * @return 已经在房间中true,否则false
//     */
//    boolean contain(P player);
//
//    /**
//     * 获取房间中的玩家集合
//     * @return 玩家集合
//     */
//    Collection<Player> getPlayers();
//
//    /**
//     * 获取玩家人数
//     * @return 玩家人数
//     */
//    int getPlayerSize();
//
//    /**
//     * 加入新玩家
//     * @param player 玩家
//     */
//    void addPlayer(P player);

    /**
     * 请求加入房间
     * @param player 请求的玩家
     * @return 加入成功true,加入失败false
     */
    boolean requestJoin(P player);
}

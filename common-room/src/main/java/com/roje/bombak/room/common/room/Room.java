package com.roje.bombak.room.common.room;

import com.google.protobuf.Message;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;


/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public interface Room<P extends Player> {

    int CARD = 1;

    int GOLD = 2;

    /**
     * 房间是否关闭
     * @return boolean
     */
    boolean isClosed();

    void setOwnerId(long creatorId);

    void setGameType(String gameType);

    /**
     * 房间id
     *
     * @return id
     */
    long getId();


    /**
     * 获取房间中指定uid的玩家
     * @param uid uid
     * @return 玩家
     */
    P getPlayer(long uid);

    /**
     * 房间总玩家数
     * @return 所有玩家的数量
     */
    int getPlayerSize();

    /**
     * 将房间数据转化为protobuf数据
     * @param player 要发送的玩家
     * @return protobuf序列化的数据
     */
    Message roomData(P player);

    /**
     * 房间任务执行器
     * @return {@link EventExecutor}
     */
    EventExecutor getExecutor();

    /**
     * 房间消息发送类
     * @return {@link RoomMessageSender}
     */
    RoomMessageSender getSender();

    /**
     * 设置房间消息发送
     * @param sender sender
     */
    void setSender(RoomMessageSender sender);

    /**
     * 设置房间任务执行器
     * @param executor 执行器
     */
    void setExecutor(EventExecutor executor);

    /**
     * 设置房间容量
     * @param capacity 容量
     */
    void setCapacity(int capacity);

    /**
     * 房间容量
     * @return 房间容量
     */
    int getCapacity();

    /**
     * 房间解散时,投票等待时间
     * @param waitVoteTime 单位:毫秒
     */
    void setWaitVoteTime(long waitVoteTime);

    /**
     * 获取房间类型
     * @return 房间类型
     */
    int getRoomType();

    /**
     * 玩家重新进入半途退出的房间
     * @param player 玩家
     * @param sessionId 玩家sessionId
     */
    void reJoin(P player, String sessionId);

    /**
     * 玩家在房间中上线
     * @param player 上线的玩家
     * @param sessionId 玩家的sessionId
     */
    void online(P player, String sessionId);

    /**
     * 新加入房间
     * @param uid 玩家uid
     * @param sessionId 玩家sessionId
     */
    void join(long uid,String sessionId);
}

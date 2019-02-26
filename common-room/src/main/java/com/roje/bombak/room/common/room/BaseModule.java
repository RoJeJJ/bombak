package com.roje.bombak.room.common.room;

import com.google.protobuf.Any;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;

import java.util.Collection;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/20
 **/
public abstract class BaseModule<P extends Player,R extends Room<P>> implements GameModule<P,R> {

    private R room;

    private EventExecutor executor;

    private RoomManager<P,R> roomManager;

    private RoomMessageSender sender;

    @Override
    public R getRoom() {
        return room;
    }

    @Override
    public void setRoom(R room) {
        this.room = room;
    }

    @Override
    public EventExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(EventExecutor executor) {
        this.executor = executor;
    }





    /**
     * 创建新玩家
     * @param uid 玩家id
     * @return 新的玩家
     */
    protected abstract P newPlayer(long uid);
}

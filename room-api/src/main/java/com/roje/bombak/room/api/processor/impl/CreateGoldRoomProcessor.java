package com.roje.bombak.room.api.processor.impl;

import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.room.Room;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
public class CreateGoldRoomProcessor<P extends Player,R extends Room<P>> implements CommonProcessor {

    private final RoomManager<P,R> roomManager;

    public CreateGoldRoomProcessor(RoomManager<P, R> roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void process(InnerClientMessage message) throws Exception {
        roomManager.createRoom(message, RoomMsg.RoomType.gold);
    }
}

package com.roje.bombak.room.api.processor.impl;

import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.room.Room;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Slf4j
public class CreateCardRoomProcessor<P extends Player,R extends Room<P>> implements CommonProcessor {

    private final RoomManager<P,R> roomManager;

    public CreateCardRoomProcessor(RoomManager<P, R> roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void process(InnerClientMessage message) throws Exception {
        roomManager.createRoom(message, RoomMsg.RoomType.card);
    }
}

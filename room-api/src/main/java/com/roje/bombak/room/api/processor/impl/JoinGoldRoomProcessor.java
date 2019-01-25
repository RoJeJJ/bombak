package com.roje.bombak.room.api.processor.impl;

import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.room.Room;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Slf4j
public class JoinGoldRoomProcessor<P extends Player,R extends Room<P>> implements CommonProcessor {

    private final RoomManager<P,R> roomManager;

    private final RoomMessageSender sender;

    public JoinGoldRoomProcessor(RoomManager<P, R> roomManager, RoomMessageSender sender) {
        this.roomManager = roomManager;
        this.sender = sender;
    }

    @Override
    public void process(InnerClientMessage message) throws Exception {
        RoomMsg.JoinRoomReq joinRoomReq = RoomMsg.JoinRoomReq.parseFrom(message.getContent());
        R room = roomManager.getGoldRoom(joinRoomReq.getRoomId());
        if (room == null) {
            log.info("没有找到金币房{}", joinRoomReq.getRoomId());
            sender.sendError(message, Constant.ErrorCode.ROOM_NOT_FOUND);
            return;
        }
        roomManager.joinRoom(message,room);
    }
}

package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.processor.RecForwardClientMessageProcessor;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Component
@Message(id = RoomConstant.Cmd.CREATE_CARD_ROOM_REQ)
public class CreateNnCardRoomProcessor implements RecForwardClientMessageProcessor {

    private final RoomManager<NnPlayer,NnRoom> roomManager;

    public CreateNnCardRoomProcessor(RoomManager<NnPlayer, NnRoom> roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void process(ServerMsg.ForwardClientMessage message) throws Exception {
        roomManager.createRoom(message);
    }
}

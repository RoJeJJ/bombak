package com.roje.bombak.nn.processor;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.processor.impl.CreateCardRoomProcessor;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Component
@Message(id = Constant.Cmd.CREATE_CARD_ROOM_REQ)
public class CreateNnCardRoomProcessor extends CreateCardRoomProcessor<NnPlayer, NnRoom> {

    public CreateNnCardRoomProcessor(RoomManager<NnPlayer, NnRoom> roomManager) {
        super(roomManager);
    }
}

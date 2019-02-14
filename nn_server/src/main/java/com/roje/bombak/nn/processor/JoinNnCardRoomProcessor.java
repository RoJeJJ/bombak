package com.roje.bombak.nn.processor;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.processor.impl.JoinCardRoomProcessor;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Component
@Message(id = Constant.Cmd.JOIN_CARD_ROOM_REQ)
public class JoinNnCardRoomProcessor extends JoinCardRoomProcessor<NnPlayer, NnRoom> {
    public JoinNnCardRoomProcessor(RoomManager<NnPlayer, NnRoom> roomManager, RoomMessageSender sender) {
        super(roomManager, sender);
    }
}

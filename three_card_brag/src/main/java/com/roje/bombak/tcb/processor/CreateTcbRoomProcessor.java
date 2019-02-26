package com.roje.bombak.tcb.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.executor.RoomCreateExecutor;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.processor.CreateRoomProcessor;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import com.roje.bombak.tcb.player.TcbPlayer;
import com.roje.bombak.tcb.room.TcbRoom;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/21
 **/
@Component
@Message(id = RoomConstant.Cmd.CREATE_ROOM_REQ)
public class CreateTcbRoomProcessor extends CreateRoomProcessor<TcbPlayer, TcbRoom> {
    public CreateTcbRoomProcessor(RoomManager<TcbPlayer, TcbRoom> roomManager,
                                  RoomCreateExecutor roomCreateExecutor,
                                  RoomProperties roomProperties,
                                  RoomMessageSender sender) {
        super(roomManager, roomCreateExecutor, roomProperties, sender, serviceInfo);
    }
}

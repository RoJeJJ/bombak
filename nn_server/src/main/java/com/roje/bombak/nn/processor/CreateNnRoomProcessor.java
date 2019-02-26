package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.executor.RoomCreateExecutor;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.processor.CreateRoomProcessor;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Slf4j
@Component
@Message(id = RoomConstant.Cmd.CREATE_ROOM_REQ)
public class CreateNnRoomProcessor extends CreateRoomProcessor<NnPlayer,NnRoom> {
    public CreateNnRoomProcessor(RoomManager<NnPlayer, NnRoom> roomManager,
                                 RoomCreateExecutor roomCreateExecutor,
                                 RoomProperties roomProperties,
                                 RoomMessageSender sender,
                                 ServiceInfo serviceInfo) {
        super(roomManager, roomCreateExecutor, roomProperties, sender, serviceInfo);
    }
}

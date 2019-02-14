package com.roje.bombak.nn.component;

import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.dispatcher.Dispatcher;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.processor.RoomProcessor;
import com.roje.bombak.room.api.utils.RabbitMessageHandler;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import org.springframework.stereotype.Component;

@Component
public class NnRabbitMessageHandler extends RabbitMessageHandler<NnPlayer, NnRoom> {
    public NnRabbitMessageHandler(Dispatcher<CommonProcessor> dispatcher,
                                  Dispatcher<RoomProcessor<NnPlayer, NnRoom>> roomMsgDispatcher,
                                  RoomManager<NnPlayer, NnRoom> roomManager, RoomMessageSender sender) {
        super(dispatcher, roomMsgDispatcher, roomManager, sender);
    }
}

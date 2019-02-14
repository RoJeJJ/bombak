package com.roje.bombak.nn.component;

import com.roje.bombak.common.api.dispatcher.Dispatcher;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.processor.RoomProcessor;
import org.springframework.stereotype.Component;

@Component
public class NnRoomDispatcher extends Dispatcher<RoomProcessor<NnPlayer, NnRoom>> {
}

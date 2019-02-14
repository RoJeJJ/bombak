package com.roje.bombak.room.api.rabbit;

import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.dispatcher.Dispatcher;
import com.roje.bombak.common.api.mq.Receiver;

public class RoomMessageReceiver extends Receiver {

    public RoomMessageReceiver(Dispatcher<CommonProcessor> dispatcher) {
        super(dispatcher);
    }
}

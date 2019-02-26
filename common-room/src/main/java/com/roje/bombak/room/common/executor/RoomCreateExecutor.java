package com.roje.bombak.room.common.executor;

import com.roje.bombak.common.thread.NamedThreadFactory;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;


/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public class RoomCreateExecutor {

    private final EventExecutor eventExecutor;

    public RoomCreateExecutor() {
        this.eventExecutor = new DefaultEventExecutor(new NamedThreadFactory("cr"));
    }

    public EventExecutor executor() {
        return eventExecutor;
    }
}

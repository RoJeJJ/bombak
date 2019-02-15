package com.roje.bombak.room.common.executor;

import com.roje.bombak.common.executor.AbstractExecutorGroup;
import com.roje.bombak.common.thread.NamedThreadFactory;
import io.netty.util.concurrent.EventExecutor;


/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
public class JoinRoomExecutorGroup extends AbstractExecutorGroup<Long> {


    public JoinRoomExecutorGroup(int size) {
        super(size, new NamedThreadFactory("join"));
    }

    @Override
    public EventExecutor selectExecutor(Long uid) {
        return executors[(int) (uid % executors.length)];
    }
}

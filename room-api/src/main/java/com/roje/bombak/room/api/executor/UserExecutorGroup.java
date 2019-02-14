package com.roje.bombak.room.api.executor;

import com.roje.bombak.common.api.executor.AbstractExecutorGroup;
import com.roje.bombak.common.api.thread.NamedThreadFactory;
import io.netty.util.concurrent.EventExecutor;


/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
public class UserExecutorGroup extends AbstractExecutorGroup<Long> {


    public UserExecutorGroup(int size) {
        super(size, new NamedThreadFactory("user"));
    }

    @Override
    public EventExecutor selectExecutor(Long uid) {
        return executors[(int) (uid % executors.length)];
    }
}

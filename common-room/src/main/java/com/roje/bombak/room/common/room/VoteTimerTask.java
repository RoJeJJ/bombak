package com.roje.bombak.room.common.room;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/20
 **/
public class VoteTimerTask {

    private long startTime;

    private long waitTime;

    private ScheduledFuture future;

    public VoteTimerTask(long waitTime, EventExecutor executor, Runnable task) {
        startTime = System.currentTimeMillis();
        this.waitTime = waitTime;
        future = executor.schedule(task,waitTime, TimeUnit.MILLISECONDS);
    }

    public void endTask() {
        future.cancel(false);
    }

}

package com.roje.bombak.common.executor;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.Arrays;
import java.util.concurrent.ThreadFactory;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/5
 **/
public abstract class AbstractExecutorGroup<R> {

    protected EventExecutor[] executors;

    protected EventExecutorGroup executorGroup;

    public AbstractExecutorGroup(int size, ThreadFactory factory) {
        if (size <= 0) {
            int s = Runtime.getRuntime().availableProcessors();
            size = s > 1 ? s - 1 : s;
        }
        executors = new EventExecutor[size];
        Arrays.fill(executors, new DefaultEventExecutor(factory));
    }

    /**
     * 自定义线程匹配规则
     * @param r r
     * @return 线程
     */
    public abstract EventExecutor selectExecutor(R r);
}

package com.roje.bombak.common.api.session;


import io.netty.util.concurrent.EventExecutor;

/**
 * @author pc
 */
public interface Session {
    /**
     * 用户id
     * @return id
     */
    long getUid();

    /**
     * 设置session的用户id
     * @param uid 用户id
     */
    void setUid(long uid);

    /**
     * 获取session执行线程
     * @return 执行线程
     */
    EventExecutor executor();
}

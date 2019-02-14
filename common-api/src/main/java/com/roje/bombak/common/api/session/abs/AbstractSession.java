package com.roje.bombak.common.api.session.abs;

import com.roje.bombak.common.api.session.Session;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/28
 **/
public abstract class AbstractSession implements Session {

    protected long uid;

    private EventExecutor executor;

    public AbstractSession(long uid,EventExecutor executor){
        this.uid = uid;
        this.executor = executor;
    }
}

package com.roje.bombak.common.dispatcher;

import com.roje.bombak.common.message.InnerClientMessage;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface CommonProcessor {
    /**
     * 消息处理器
     * @param message 内部消息
     * @throws Exception 处理异常
     */
    void process(InnerClientMessage message) throws Exception;
}

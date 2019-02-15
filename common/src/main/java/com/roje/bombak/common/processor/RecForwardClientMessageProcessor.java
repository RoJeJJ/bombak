package com.roje.bombak.common.processor;

import com.roje.bombak.common.proto.ServerMsg;

/**
 * 接收转发客户端消息的处理器
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface RecForwardClientMessageProcessor {
    /**
     * 消息处理器
     * @param message 消息
     * @throws Exception 处理异常
     */
    void process(ServerMsg.ForwardClientMessage message) throws Exception;
}

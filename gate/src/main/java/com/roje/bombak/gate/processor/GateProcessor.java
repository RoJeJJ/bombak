package com.roje.bombak.gate.processor;


import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.gate.session.GateSession;


/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
public interface GateProcessor {
    /**
     * 网关消息处理方法
     * @param session 网关session
     * @param message 消息包
     * @throws Exception 处理异常
     */
    void process(GateSession session, ServerMsg.C2SMessage message) throws Exception;
}

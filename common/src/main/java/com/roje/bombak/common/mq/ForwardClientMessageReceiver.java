package com.roje.bombak.common.mq;

import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.processor.RecForwardClientMessageProcessor;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.proto.ServerMsg;
import lombok.extern.slf4j.Slf4j;

/**
 * rabbit
 * 转发客户端消息接收器
 * @author pc
 */
@Slf4j
public class ForwardClientMessageReceiver {

    protected final Dispatcher dispatcher;

    public ForwardClientMessageReceiver(Dispatcher dispatcher) {

        this.dispatcher = dispatcher;
    }

    /**
     * 从byte数组中解析出其他服务器转发过来的客户端消息
     * @param data 消息数据
     * @return 其他服务器转发过来的客户端消息
     */
    protected ServerMsg.ForwardClientMessage parseClientMessage(byte[] data) {
        ServerMsg.ForwardClientMessage message = null;
        try {
            message = ServerMsg.ForwardClientMessage.parseFrom(data);
        }catch (InvalidProtocolBufferException e) {
            log.warn("解析消息异常",e);
        }
        return message;
    }

    /**
     * 自行处理转发过来的客户端消息
     * @param message 其他服务器转发过来的消息
     */
    protected void process(ServerMsg.ForwardClientMessage message) {
        RecForwardClientMessageProcessor processor = dispatcher.processor(message.getCsMessage().getMessageId());
        if (processor == null) {
            log.warn("不支持的消息,消息号：{}",message.getCsMessage().getMessageId());
        } else {
            try {
                processor.process(message);
            } catch (Exception e) {
                log.warn("处理任务异常",e);
            }
        }
    }
}

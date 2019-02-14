package com.roje.bombak.common.api.mq;

import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.dispatcher.Dispatcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Receiver {

    private final Dispatcher<CommonProcessor> dispatcher;

    public Receiver(Dispatcher<CommonProcessor> dispatcher) {

        this.dispatcher = dispatcher;
    }

    protected void innerC2SMessage(byte[] data) throws Exception {
        ServerMsg.InnerC2SMessage message = ServerMsg.InnerC2SMessage.parseFrom(data);
        CommonProcessor processor = dispatcher.processor(message.getCsMessage().getMessageId());
        if (processor == null) {
            log.warn("不支持的消息,消息号：{}",message.getCsMessage().getMessageId());
        } else {
            processor.process(message);
        }
    }
}

package com.roje.bombak.lobby.rabbit;

import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.dispatcher.Dispatcher;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.common.api.mq.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
public class LobbyReceiver extends Receiver {

    public LobbyReceiver(Dispatcher<CommonProcessor> dispatcher) {
        super(dispatcher);
    }

    @RabbitListener(queues = "lobby-1")
    public void onMessage(byte[] data) {
        try {
            innerC2SMessage(data);
        } catch (Exception e) {
            log.info("大厅消息处理消息异常", e);
        }
    }
}

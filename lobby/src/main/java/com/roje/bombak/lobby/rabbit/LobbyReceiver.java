package com.roje.bombak.lobby.rabbit;

import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.dispatcher.Dispatcher;
import com.roje.bombak.common.message.InnerClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
public class LobbyReceiver {

    private final Dispatcher<CommonProcessor> dispatcher;

    LobbyReceiver(Dispatcher<CommonProcessor> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @RabbitListener(queues = "lobby-1")
    public void onMessage(InnerClientMessage message) {
        System.out.println(message);
        CommonProcessor processor = dispatcher.processor(message.getMessageId());
        if (processor != null) {
            try {
                processor.process(message);
            } catch (Exception e) {
                log.info("大厅消息处理消息异常,消息号:"+message.getMessageId(), e);
            }
        } else {
            log.warn("消息号:{},没有被处理",message.getMessageId());
        }
    }
}

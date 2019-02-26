package com.roje.bombak.lobby.rabbit;

import com.roje.bombak.common.mq.GateToServerMessageReceiver;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.proto.ServerMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
public class LobbyClientMessageReceiver extends GateToServerMessageReceiver {

    public LobbyClientMessageReceiver(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @RabbitListener(queues = "lobby-1")
    public void onMessage(byte[] data) {
        ServerMsg.GateToServerMessage message = parseMessage(data);
        if (message != null) {
            process(message);
        }
    }
}

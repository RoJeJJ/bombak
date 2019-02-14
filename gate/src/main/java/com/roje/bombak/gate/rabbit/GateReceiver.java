package com.roje.bombak.gate.rabbit;

import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
public class GateReceiver {

    private final GateSessionManager serverManager;


    public GateReceiver(GateSessionManager serverManager) {
        this.serverManager = serverManager;
    }

    @RabbitListener(queues = "gate-1")
    public void onMessage(byte[] data) {
        ServerMsg.InnerS2CMessage message;
        try {
            message = ServerMsg.InnerS2CMessage.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return;
        }

        GateSession s = serverManager.getSession(message.getUid());
        if (s != null) {
            s.send(message.getScMessage());
        }
    }
}

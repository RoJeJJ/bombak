package com.roje.bombak.gate.rabbit;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.proto.Gate;
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
    public void onMessage(InnerClientMessage message) {
        GateSession s = serverManager.getSession(message.getUid());
        if (s != null) {
            log.info("收到回复消息{},转发给用户",message);
            Gate.ServerMessage.Builder builder = Gate.ServerMessage.newBuilder();
            builder.setMessageId(message.getMessageId());
            builder.setServiceType(message.getSenderServiceType());
            builder.setTimestamp(message.getTimestamp());
            builder.setErrorCode(message.getErrorCode());
            if (message.getContent() != null && message.getContent().length > 0) {
                builder.setData(ByteString.copyFrom(message.getContent()));
            }
            s.send(builder.build());
//            s.executor().execute(() -> {
//                GateSession session = serverManager.getSession(message.getUid());
//                if (session != null) {
//                    log.info("收到回复消息,转发给用户");
//                    session.send(resp);
//                }
//            });
        }
    }
}

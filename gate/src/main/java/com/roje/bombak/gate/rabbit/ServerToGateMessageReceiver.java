package com.roje.bombak.gate.rabbit;

import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 接收回复给客户端的消息
 * @author pc
 */
@Slf4j
@Component
public class ServerToGateMessageReceiver {

    private final GateSessionManager serverManager;


    public ServerToGateMessageReceiver(GateSessionManager serverManager) {
        this.serverManager = serverManager;
    }

    @RabbitListener(queues = "gate-1")
    public void onMessage(byte[] data) {
        ServerMsg.ServerToGateMessage message;
        try {
            message = ServerMsg.ServerToGateMessage.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            log.warn("消息解析异常",e);
            return;
        }

        GateSession s = serverManager.getSession(message.getSessionId());
        if (s != null) {
            ServerMsg.GateToClientMessage.Builder builder = ServerMsg.GateToClientMessage.newBuilder();
            builder.setMsgType(message.getMsgType())
                    .setMsgId(message.getMsgId())
                    .setErrCode(message.getErrCode())
                    .setData(message.getData());
            s.send(builder.build());
        }
    }
}

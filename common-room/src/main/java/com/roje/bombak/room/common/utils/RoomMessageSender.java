package com.roje.bombak.room.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.player.Player;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.Collection;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/14
 **/
public class RoomMessageSender extends MessageSender {

    public RoomMessageSender(ServiceInfo serviceInfo, AmqpTemplate amqpTemplate) {
        super(serviceInfo, amqpTemplate);
    }

    public  <P extends Player>void sendMsgToGate(P player, int messageId, Message message) {
        if (!player.isOffline() && !player.isExit()) {
            ServerMsg.ServerToGateMessage.Builder builder = ServerMsg.ServerToGateMessage.newBuilder();
            builder.setMsgId(messageId)
                    .setMsgType(RoomConstant.ROOM_CMD)
                    .setSessionId(player.getSessionId())
                    .setData(Any.pack(message));
            send(player,builder.build());
        }
    }

    public  <P extends Player>void sendMsgToGate(P player, int messageId) {
        if (!player.isOffline() && !player.isExit()) {
            ServerMsg.ServerToGateMessage.Builder builder = ServerMsg.ServerToGateMessage.newBuilder();
            builder.setMsgId(messageId)
                    .setMsgType(RoomConstant.ROOM_CMD)
                    .setSessionId(player.getSessionId());
            send(player,builder.build());
        }
    }

    private  <P extends Player>void  send(P player, ServerMsg.ServerToGateMessage message) {
        ServiceInfo gateInfo = player.getGateInfo();
        String key = gateInfo.getServiceType() + "-" + gateInfo.getServiceId();
        amqpTemplate.convertAndSend(key,message.toByteArray());
    }

    public <P extends Player>void sendMsgToGate(Collection<P> players, int messageId) {
        for (P p:players) {
            sendMsgToGate(p,messageId);
        }
    }

    public <P extends Player>void sendMsgToGate(Collection<P> players, int messageId, Message message) {
        for (Player p : players) {
            sendMsgToGate(p,messageId,message);
        }
    }

    public <P extends Player>void sendErrMsgToGate(P player, int error) {
        if (!player.isOffline() && !player.isExit()) {
            ServerMsg.ServerToGateMessage.Builder builder = ServerMsg.ServerToGateMessage.newBuilder();
            builder.setErrCode(error);
            send(player,builder.build());
        }
    }
}

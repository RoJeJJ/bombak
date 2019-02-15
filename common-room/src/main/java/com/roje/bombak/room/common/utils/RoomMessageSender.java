package com.roje.bombak.room.common.utils;

import com.google.protobuf.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
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

    public  <P extends Player>void sendMsg(P player, int messageId, Message message) {
        if (!player.isOffline() && !player.isExit()) {
            ServerMsg.S2CMessage sc;
            if (message == null) {
                sc = scMsg(messageId);
            } else {
                sc = scMsg(messageId,message);
            }
            send(player,sc);
        }
    }

    private  <P extends Player>void send(P player, ServerMsg.S2CMessage s2CMessage) {
        ServerMsg.ReplyClientMessage replyMsg = replyClientMsg(s2CMessage,player.uid());
        String routeKey = player.getServiceType() + "-" + player.getServiceId();
        amqpTemplate.convertAndSend(routeKey, replyMsg.toByteArray());
    }

    public <P extends Player>void sendMsg(P player, int messageId) {
        sendMsg(player, messageId, null);
    }

    public <P extends Player> void sendMsg(Collection<P> players, int messageId) {
        sendMsg(players,messageId,null);
    }

    public <P extends Player> void sendMsg(Collection<P> players, int messageId, Message message) {
        for (Player p : players) {
            sendMsg(p, messageId, message);
        }
    }

    public <P extends Player> void sendErrMsg(P player,  int error) {
        if (!player.isOffline() && !player.isExit()) {
            ServerMsg.S2CMessage sc = scErrMsg(error);
            send(player,sc);
        }
    }
}

package com.roje.bombak.room.api.utils;

import com.roje.bombak.common.api.eureka.ServiceInfo;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.common.api.utils.MessageSender;
import com.roje.bombak.room.api.player.Player;
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

    public <P extends Player>void send(P player, int messageId, byte[] message) {
        if (!player.isOffline() && !player.isExit()) {
            InnerClientMessage resp = generateInnerMessage(player.uid(), messageId);
            if (message != null) {
                resp.setContent(message);
            }
            String routeKey = player.getServiceType() + "-" + player.getServiceId();
            amqpTemplate.convertAndSend(routeKey, resp);
        }
    }

    public <P extends Player>void send(P player, int messageId) {
        send(player, messageId, null);
    }

    public <P extends Player> void send(Collection<P> players, int messageId) {
        send(players,messageId,null);
    }

    public <P extends Player> void send(Collection<P> players, int messageId, byte[] message) {
        for (Player p : players) {
            send(p, messageId, message);
        }
    }

    public <P extends Player> void sendError(P player, int messageId, int error) {
        if (!player.isOffline()) {
            InnerClientMessage resp = generateInnerMessage(player.uid(), messageId);
            resp.setErrorCode(error);
            String routeKey = player.getServiceType() + "-" + player.getServiceId();
            amqpTemplate.convertAndSend(routeKey, resp);
        }
    }

}

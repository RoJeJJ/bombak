package com.roje.bombak.common.utils;

import com.google.protobuf.Message;
import com.roje.bombak.common.constant.GlobalConstant;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import org.springframework.amqp.core.AmqpTemplate;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/10
 **/
public class MessageSender {

    private static ServiceInfo serviceInfo;

    protected static AmqpTemplate amqpTemplate;

    public MessageSender(ServiceInfo serviceInfo, AmqpTemplate amqpTemplate) {
        MessageSender.serviceInfo = serviceInfo;
        MessageSender.amqpTemplate = amqpTemplate;
    }

    public void sendFanoutMessage(long uid, int messageId) {
        InnerClientMessage clientMessage = generateInnerMessage(uid,messageId);
        amqpTemplate.convertAndSend(GlobalConstant.BROADCAST_EXCHANGE_NAME,clientMessage);
    }

    public void send(InnerClientMessage message, int messageId, Message proto) {
        InnerClientMessage resp = generateInnerMessage(message.getUid(), messageId);
        resp.setContent(proto.toByteArray());
        String routeKey = message.getSenderServiceType() + "-" + message.getSenderServiceId();
        amqpTemplate.convertAndSend(routeKey,resp);
    }

    public void sendError(InnerClientMessage message, int errorCode) {
        InnerClientMessage resp = generateInnerMessage(message.getUid(), message.getMessageId());
        resp.setErrorCode(errorCode);
        String routeKey = message.getSenderServiceType() + "-" + message.getSenderServiceId();
        amqpTemplate.convertAndSend(routeKey,resp);
    }

    protected InnerClientMessage generateInnerMessage(long uid, int messageId) {
        InnerClientMessage resp = new InnerClientMessage();
        resp.setTimestamp(System.currentTimeMillis());
        resp.setSenderServiceType(serviceInfo.getServiceType());
        resp.setSenderServiceId(serviceInfo.getServiceId());
        resp.setUid(uid);
        resp.setMessageId(messageId);
        return resp;
    }
}

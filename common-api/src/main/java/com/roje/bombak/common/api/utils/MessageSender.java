package com.roje.bombak.common.api.utils;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.common.api.eureka.ServiceInfo;
import com.roje.bombak.common.api.constant.GlobalConstant;
import com.roje.bombak.common.api.message.InnerClientMessage;
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
        ServerMsg.S2CMessage.Builder scb = scBuilder(messageId);
        scb.setData(Any.pack(proto));
        ServerMsg.InnerS2CMessage.Builder iscb = innerScBuilder(scb,message.getUid());
        String routeKey = message.getSenderServiceType() + "-" + message.getSenderServiceId();
        amqpTemplate.convertAndSend(routeKey,iscb.build().toByteArray());
    }

    public void sendError(ServerMsg.InnerC2SMessage message, int errorCode) {
        ServerMsg.S2CMessage.Builder scb = scBuilder(message.getCsMessage().getMessageId());
        scb.setErrorCode(errorCode);
        ServerMsg.InnerS2CMessage.Builder iscb = innerScBuilder(scb,message.getUid());
        String routeKey = message.getSenderType() + "-" + message.getSenderId();
        amqpTemplate.convertAndSend(routeKey,iscb.build().toByteArray());
    }

    private ServerMsg.S2CMessage.Builder scBuilder(int messageId) {
        ServerMsg.S2CMessage.Builder scBuilder = ServerMsg.S2CMessage.newBuilder();
        scBuilder.setTimestamp(System.currentTimeMillis())
                .setMessageId(messageId);
        return scBuilder;
    }

    private ServerMsg.InnerS2CMessage.Builder innerScBuilder(ServerMsg.S2CMessage.Builder b,long uid) {
        ServerMsg.InnerS2CMessage.Builder builder = ServerMsg.InnerS2CMessage.newBuilder();
        builder.setScMessage(b)
                .setUid(uid)
                .setSenderType(serviceInfo.getServiceType())
                .setSenderId(serviceInfo.getServiceId());
        return builder;
    }

    public ServerMsg.S2CMessage buildErrorMessage(int messageId, int errorCode) {
        ServerMsg.S2CMessage.Builder builder = generalMessage(messageId);
        builder.setErrorCode(errorCode);
        return builder.build();
    }

    public ServerMsg.S2CMessage buildMessage(int messageId, Message proto) {
        ServerMsg.S2CMessage.Builder resp = generalMessage(messageId);
        resp.setData(Any.pack(proto));
        return resp.build();
    }

    public ServerMsg.S2CMessage buildMessage(int messageId) {
        return generalMessage(messageId).build();
    }

    private ServerMsg.S2CMessage.Builder generalMessage(int messageId) {
        ServerMsg.S2CMessage.Builder builder = ServerMsg.S2CMessage.newBuilder();
        builder.setMessageId(messageId)
                .setTimestamp(System.currentTimeMillis());
        return builder;
    }
}

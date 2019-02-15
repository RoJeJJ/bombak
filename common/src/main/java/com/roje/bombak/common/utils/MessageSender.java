package com.roje.bombak.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.constant.GlobalConstant;
import com.roje.bombak.common.proto.ServerMsg;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.cloud.client.ServiceInstance;

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

    /**
     * 广播消息
     */
    public void broadCastMessage(long uid, int messageId) {
        ServerMsg.ServerBroadcastMessage.Builder builder = ServerMsg.ServerBroadcastMessage.newBuilder();
        builder.setUid(uid)
                .setMessageId(messageId)
                .setServerType(serviceInfo.getServiceType())
                .setServerId(serviceInfo.getServiceId());
        amqpTemplate.convertAndSend(GlobalConstant.BROADCAST_EXCHANGE_NAME,builder.build().toByteArray());
    }

    /**
     * 转发回复给客户端的消息
     */
    public void replyClientMsg(ServerMsg.ForwardClientMessage message, int messageId, Message proto) {
        ServerMsg.S2CMessage sc = scMsg(messageId,proto);
        ServerMsg.ReplyClientMessage replyMsg = replyClientMsg(sc,message.getUid());
        forward(message,replyMsg);
    }

    /**
     *转发一个错误消息给客户端
     */
    public void replyClientErrMsg(ServerMsg.ForwardClientMessage message, int err) {
        ServerMsg.S2CMessage sc = scErrMsg(err);
        ServerMsg.ReplyClientMessage replyMsg = replyClientMsg(sc,message.getUid());
        forward(message,replyMsg);
    }

    public ServerMsg.S2CMessage scMsg(int messageId,Message data) {
        ServerMsg.S2CMessage.Builder scBuilder = ServerMsg.S2CMessage.newBuilder();
        scBuilder.setTimestamp(System.currentTimeMillis())
                .setMessageId(messageId)
                .setData(Any.pack(data));
        return scBuilder.build();
    }

    public ServerMsg.S2CMessage scMsg(int messageId) {
        ServerMsg.S2CMessage.Builder scBuilder = ServerMsg.S2CMessage.newBuilder();
        scBuilder.setTimestamp(System.currentTimeMillis())
                .setMessageId(messageId);
        return scBuilder.build();
    }

    public ServerMsg.S2CMessage scErrMsg(int err) {
        ServerMsg.S2CMessage.Builder builder = ServerMsg.S2CMessage.newBuilder();
        builder.setTimestamp(System.currentTimeMillis())
                .setErrorCode(err);
        return builder.build();
    }

    protected ServerMsg.ReplyClientMessage replyClientMsg(ServerMsg.S2CMessage sc,long uid) {
        ServerMsg.ReplyClientMessage.Builder builder = ServerMsg.ReplyClientMessage.newBuilder();
        builder.setScMessage(sc)
                .setUid(uid)
                .setServerType(serviceInfo.getServiceType())
                .setServerId(serviceInfo.getServiceId());
        return builder.build();
    }

    private void forward(ServerMsg.ForwardClientMessage message, ServerMsg.ReplyClientMessage reply) {
        String routeKey = message.getServerType() + "-" + message.getServerId();
        amqpTemplate.convertAndSend(routeKey,reply.toByteArray());
    }

    public void amqpMessage(ServiceInfo serviceInfo,byte[] data) {
        String routeKey = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
        amqpTemplate.convertAndSend(routeKey,data);
    }

    public void amqpMessage(ServiceInstance instance,byte[] data) {
        String routeKey = instance.getServiceId() + "-" +instance.getMetadata().get("id");
        amqpTemplate.convertAndSend(routeKey,data);
    }
}

package com.roje.bombak.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.constant.Constant;
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
    public void allServerMsg(int msgType, int messageId,Message message) {
        ServerMsg.ServerToServerMessage.Builder builder = ServerMsg.ServerToServerMessage.newBuilder();
        builder.setMsgType(msgType)
                .setMsgId(messageId);
        if (message != null) {
            builder.setData(Any.pack(message));
        }
        amqpTemplate.convertAndSend(Constant.BROADCAST_EXCHANGE_NAME,builder.build().toByteArray());
    }

    public void sendMsgToGate(ServerMsg.GateToServerMessage gateMsg, int messageId, Message message) {
        ServerMsg.ServerToGateMessage.Builder builder = ServerMsg.ServerToGateMessage.newBuilder();
        builder.setMsgType(gateMsg.getMsgType())
                .setMsgId(messageId)
                .setSessionId(gateMsg.getSessionId())
                .setData(Any.pack(message));
        String gate = gateMsg.getServiceType() + "-" + gateMsg.getServiceId();
        amqpTemplate.convertAndSend(gate,message);
    }

    public void sendErrMsgToGate(ServerMsg.GateToServerMessage message, int err) {
        ServerMsg.ServerToGateMessage.Builder builder = ServerMsg.ServerToGateMessage.newBuilder();
        builder.setErrCode(err);
        String gate = message.getServiceType() + "-" + message.getServiceId();
        amqpTemplate.convertAndSend(gate,message);
    }

    public ServerMsg.GateToClientMessage scMsg(int msgType,int msgId,Message data) {
        ServerMsg.GateToClientMessage.Builder builder = ServerMsg.GateToClientMessage.newBuilder();
        builder.setMsgType(msgType)
                .setMsgId(msgId)
                .setData(Any.pack(data));
        return builder.build();
    }

    public ServerMsg.GateToClientMessage scMsg(int msgType, int messageId) {
        ServerMsg.GateToClientMessage.Builder builder = ServerMsg.GateToClientMessage.newBuilder();
        builder.setMsgType(msgType)
                .setMsgId(messageId);
        return builder.build();
    }

    public ServerMsg.GateToClientMessage scErrMsg(int err) {
        ServerMsg.GateToClientMessage.Builder builder = ServerMsg.GateToClientMessage.newBuilder();
        builder.setErrCode(err);
        return builder.build();
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

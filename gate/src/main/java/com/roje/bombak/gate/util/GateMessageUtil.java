package com.roje.bombak.gate.util;

import com.google.protobuf.Message;
import com.roje.bombak.gate.proto.Gate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 */
@Slf4j
public class GateMessageUtil {
    public static Gate.ServerMessage buildErrorMessage(Gate.ClientMessage message, int messageId, int errorCode) {
        Gate.ServerMessage.Builder resp = generalMessage(message, messageId);
        resp.setErrorCode(errorCode);
        return resp.build();
    }

    public static Gate.ServerMessage buildMessage(Gate.ClientMessage message, int messageId, Message proto) {
        Gate.ServerMessage.Builder resp = generalMessage(message, messageId);
        resp.setData(proto.toByteString());
        return resp.build();
    }

    private static Gate.ServerMessage.Builder generalMessage(Gate.ClientMessage message, int messageId) {
        Gate.ServerMessage.Builder builder = Gate.ServerMessage.newBuilder();
        builder.setSerial(message.getSerial());
        builder.setMessageId(messageId);
        builder.setServiceType(message.getServiceType());
        builder.setTimestamp(System.currentTimeMillis());
        return builder;
    }

}

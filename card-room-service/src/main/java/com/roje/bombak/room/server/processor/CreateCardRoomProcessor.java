package com.roje.bombak.room.server.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.processor.GateToServerMessageProcessor;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.proto.RoomMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
@Slf4j
@Component
@Message(id = RoomConstant.Cmd.CREATE_ROOM_REQ)
public class CreateCardRoomProcessor implements GateToServerMessageProcessor {

    private final DiscoveryClient discoveryClient;

    private final MessageSender messageSender;

    public CreateCardRoomProcessor(DiscoveryClient discoveryClient, MessageSender messageSender) {
        this.discoveryClient = discoveryClient;

        this.messageSender = messageSender;
    }

    @Override
    public void process(ServerMsg.GateToServerMessage message) throws Exception {
        RoomMsg.CreateRoomReq request = message.getData().unpack(RoomMsg.CreateRoomReq.class);
        String gameName = request.getGameName();
        if (StringUtils.isBlank(gameName)) {
            log.info("房间类型不能为空");
            messageSender.sendErrMsgToGate(message,RoomConstant.ErrorCode.ROOM_TYPE_IS_EMPTY);
            return;
        }
        ServiceInstance instance = null;
        int min = -1;
        List<ServiceInstance> instances = discoveryClient.getInstances(gameName);
        for (ServiceInstance i:instances) {
            String s = i.getMetadata().get("roomSize");
            if (StringUtils.isNumeric(s)) {
                int size = Integer.valueOf(s);
                if (size < min || min == -1 ) {
                    instance = i;
                    min = size;
                }
            }
        }
        if (instance == null) {
            log.info("没有找到该游戏类型");
            messageSender.sendErrMsgToGate(message,RoomConstant.ErrorCode.NO_SUCH_GAME);
            return;
        }
        messageSender.amqpMessage(instance,message.toByteArray());
    }
}

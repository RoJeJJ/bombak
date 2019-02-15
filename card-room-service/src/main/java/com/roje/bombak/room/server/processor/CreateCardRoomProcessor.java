package com.roje.bombak.room.server.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.processor.RecForwardClientMessageProcessor;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.proto.RoomMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
@Slf4j
@Component
@Message(id = RoomConstant.Cmd.CREATE_CARD_ROOM_REQ)
public class CreateCardRoomProcessor implements RecForwardClientMessageProcessor {

    private final LoadBalancerClient loadBalancerClient;

    private final MessageSender messageSender;

    public CreateCardRoomProcessor(LoadBalancerClient loadBalancerClient, MessageSender messageSender) {
        this.loadBalancerClient = loadBalancerClient;

        this.messageSender = messageSender;
    }

    @Override
    public void process(ServerMsg.ForwardClientMessage message) throws Exception {
        RoomMsg.CreateRoomReq request = message.getCsMessage().getData().unpack(RoomMsg.CreateRoomReq.class);
        String gameName = request.getGameName();
        if (StringUtils.isBlank(gameName)) {
            log.info("房间类型不能为空");
            messageSender.replyClientErrMsg(message,RoomConstant.ErrorCode.ROOM_TYPE_IS_EMPTY);
            return;
        }
        ServiceInstance instance = loadBalancerClient.choose(gameName);
        if (instance == null) {
            log.info("没有找到该游戏类型");
            messageSender.replyClientErrMsg(message,RoomConstant.ErrorCode.NO_SUCH_GAME);
            return;
        }
        messageSender.amqpMessage(instance,message.toByteArray());
    }
}

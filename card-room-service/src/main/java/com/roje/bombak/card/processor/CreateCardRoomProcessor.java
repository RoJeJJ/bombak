package com.roje.bombak.card.processor;

import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.common.api.utils.MessageSender;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.proto.RoomMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
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
@Message(id = Constant.Cmd.CREATE_CARD_ROOM_REQ)
public class CreateCardRoomProcessor implements CommonProcessor {

    private final LoadBalancerClient loadBalancerClient;

    private final AmqpTemplate amqpTemplate;

    private final MessageSender messageSender;

    public CreateCardRoomProcessor(LoadBalancerClient loadBalancerClient,
                                   AmqpTemplate amqpTemplate, MessageSender messageSender) {
        this.loadBalancerClient = loadBalancerClient;
        this.amqpTemplate = amqpTemplate;
        this.messageSender = messageSender;
    }

    @Override
    public void process(ServerMsg.InnerC2SMessage message) throws Exception {
        RoomMsg.CreateRoomReq request = message.getCsMessage().getData().unpack(RoomMsg.CreateRoomReq.class);
        String gameName = request.getGameName();
        if (StringUtils.isBlank(gameName)) {
            log.info("房间类型不能为空");
            messageSender.sendError(message,Constant.ErrorCode.ROOM_TYPE_IS_EMPTY);
            return;
        }
        ServiceInstance instance = loadBalancerClient.choose(gameName);
        if (instance == null) {
            log.info("没有找到该游戏类型");
            messageSender.sendError(message,Constant.ErrorCode.NO_SUCH_GAME);
            return;
        }
        String routeKey = instance.getServiceId() + "-" +instance.getMetadata().get("id");
        amqpTemplate.convertAndSend(routeKey,message.toByteArray());
    }
}

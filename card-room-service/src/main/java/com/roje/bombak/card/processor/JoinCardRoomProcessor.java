package com.roje.bombak.card.processor;

import com.roje.bombak.common.api.ServerMsg;
import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.eureka.ServiceInfo;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.common.api.utils.MessageSender;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.redis.RoomRedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/7
 **/
@Slf4j
@Component
@Message(id = Constant.Cmd.JOIN_CARD_ROOM_REQ)
public class JoinCardRoomProcessor implements CommonProcessor {

    private final RoomRedisDao roomRedisDao;

    private final AmqpTemplate amqpTemplate;

    private final MessageSender messageSender;

    public JoinCardRoomProcessor(RoomRedisDao roomRedisDao, AmqpTemplate amqpTemplate, MessageSender messageSender) {
        this.roomRedisDao = roomRedisDao;
        this.amqpTemplate = amqpTemplate;
        this.messageSender = messageSender;
    }

    @Override
    public void process(ServerMsg.InnerC2SMessage message) throws Exception {
        RoomMsg.JoinRoomReq request = message.getCsMessage().getData().unpack(RoomMsg.JoinRoomReq.class);
        long roomId = request.getRoomId();
        ServiceInfo serviceInfo = roomRedisDao.getRoomService(roomId);
        if (serviceInfo == null) {
            log.info("房间{}不存在",roomId);
            messageSender.sendError(message,Constant.ErrorCode.ROOM_NOT_FOUND);
        } else {
            String routeKey = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
            amqpTemplate.convertAndSend(routeKey,message);
        }
    }
}

package com.roje.bombak.card.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.common.utils.MessageSender;
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
@Message(id = Constant.JOIN_CARD_ROOM_REQ)
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
    public void process(InnerClientMessage message) throws Exception {
        RoomMsg.JoinRoomReq request = RoomMsg.JoinRoomReq.parseFrom(message.getContent());
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

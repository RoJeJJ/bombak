package com.roje.bombak.room.server.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.processor.GateToServerMessageProcessor;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.room.common.proto.RoomMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/7
 **/
@Slf4j
@Component
@Message(id = RoomConstant.Cmd.JOIN_ROOM_REQ)
public class JoinRoomProcessor implements GateToServerMessageProcessor {

    private final RoomRedisDao roomRedisDao;

    private final MessageSender messageSender;

    public JoinRoomProcessor(RoomRedisDao roomRedisDao, MessageSender messageSender) {
        this.roomRedisDao = roomRedisDao;
        this.messageSender = messageSender;
    }

    @Override
    public void process(ServerMsg.GateToServerMessage message) throws Exception {
        RoomMsg.JoinRoomReq request = message.getData().unpack(RoomMsg.JoinRoomReq.class);
        long roomId = request.getRoomId();
        ServiceInfo requestServiceInfo = roomRedisDao.getRoomService(roomId);
        ServiceInfo userJoinedServiceInfo = roomRedisDao.getUserRoomService(message.getUserId());
        if (userJoinedServiceInfo != null) {
            messageSender.amqpMessage(userJoinedServiceInfo,message.toByteArray());
        } else if (requestServiceInfo != null) {
            messageSender.amqpMessage(requestServiceInfo,message.toByteArray());
        } else {
            messageSender.sendErrMsgToGate(message,RoomConstant.ErrorCode.ROOM_NOT_FOUND);
        }
    }
}

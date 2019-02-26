package com.roje.bombak.room.server.rabbit;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.mq.GateToServerMessageReceiver;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
@Slf4j
@Component
public class GateToRoomServiceMessageReceiver extends GateToServerMessageReceiver {

    private final RoomRedisDao roomRedisDao;

    private final AmqpTemplate amqpTemplate;

    public GateToRoomServiceMessageReceiver(Dispatcher dispatcher,
                                       RoomRedisDao roomRedisDao,
                                       AmqpTemplate amqpTemplate) {
        super(dispatcher);
        this.roomRedisDao = roomRedisDao;
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues = "room-service-1")
    public void onMessage(byte[] data) {
        ServerMsg.GateToServerMessage message = parseMessage(data);
        if (message == null) {
            return;
        }
        if (message.getMsgType() == RoomConstant.ROOM_SERVICE) {
            process(message);
        } else if (message.getMsgType() == RoomConstant.ROOM_CMD){
            ServiceInfo serviceInfo = roomRedisDao.getUserRoomService(message.getUserId());
            if (serviceInfo == null) {
                log.info("没有加入任何房间");
            } else {
                String routeKey = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
                amqpTemplate.convertAndSend(routeKey,message);
            }
        }
    }
}

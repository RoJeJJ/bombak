package com.roje.bombak.room.server.rabbit;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.mq.ForwardClientMessageReceiver;
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
public class RoomServiceReceiver extends ForwardClientMessageReceiver {

    private final RoomRedisDao roomRedisDao;

    private final AmqpTemplate amqpTemplate;

    public RoomServiceReceiver(Dispatcher dispatcher,
                               RoomRedisDao roomRedisDao,
                               AmqpTemplate amqpTemplate) {
        super(dispatcher);
        this.roomRedisDao = roomRedisDao;
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues = "room-service-1")
    public void onMessage(byte[] data) {
        ServerMsg.ForwardClientMessage message = parseClientMessage(data);
        if (message == null) {
            return;
        }
        int messageId = message.getCsMessage().getMessageId();
        if (messageId != RoomConstant.Cmd.ROOM_CMD) {
            process(message);
        } else {
            ServiceInfo serviceInfo = roomRedisDao.getUserRoomService(message.getUid());
            if (serviceInfo == null) {
                log.info("没有加入任何房间");
            } else {
                String routeKey = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
                amqpTemplate.convertAndSend(routeKey,message);
            }
        }
    }
}

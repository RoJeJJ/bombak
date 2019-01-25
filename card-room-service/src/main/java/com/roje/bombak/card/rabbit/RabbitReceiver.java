package com.roje.bombak.card.rabbit;

import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.dispatcher.Dispatcher;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.redis.RoomRedisDao;
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
public class RabbitReceiver {

    private final Dispatcher<CommonProcessor> dispatcher;

    private final RoomRedisDao roomRedisDao;

    private final AmqpTemplate amqpTemplate;

    public RabbitReceiver(Dispatcher<CommonProcessor> dispatcher,
                          RoomRedisDao roomRedisDao, AmqpTemplate amqpTemplate) {
        this.dispatcher = dispatcher;
        this.roomRedisDao = roomRedisDao;
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues = "room-service-1")
    public void onMessage(InnerClientMessage message) {
        CommonProcessor processor = dispatcher.processor(message.getMessageId());
        if (processor != null) {
            try {
                processor.process(message);
            } catch (Exception e) {
                log.info("消息处理消息异常,消息号:"+message.getMessageId(), e);
            }
        } else {
            ServiceInfo serviceInfo = roomRedisDao.getUserRoomService(message.getUid());
            if (serviceInfo == null) {
                log.info("没有加入任何房间");
            } else {
                message.setMessageId(- message.getMessageId());
                String routeKey = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
                amqpTemplate.convertAndSend(routeKey,message);
            }
        }
    }
}

package com.roje.bombak.nn.rabbit;
import com.roje.bombak.common.constant.GlobalConstant;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.nn.component.NnRabbitMessageHandler;
import com.roje.bombak.room.api.manager.RoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/14
 **/
@Slf4j
@Component
public class NnCardReceiver {

    private final NnRabbitMessageHandler rabbitMessageHandler;

    public NnCardReceiver(NnRabbitMessageHandler rabbitMessageHandler) {
        this.rabbitMessageHandler = rabbitMessageHandler;
    }

    @RabbitListener(queues = "nn-1")
    public void onMessage(InnerClientMessage message) {
        rabbitMessageHandler.handle(message);
    }

//    @RabbitListener(queues = GlobalConstant.BROADCAST_QUEUE_NAME)
//    public void onFanoutMessage(InnerClientMessage message) {
//        roomManager.onFanoutMessage(message);
//    }
}

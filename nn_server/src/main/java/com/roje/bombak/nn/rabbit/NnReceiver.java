package com.roje.bombak.nn.rabbit;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.nn.component.NnRabbitMessageHandler;
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
public class NnReceiver {

    private final NnRabbitMessageHandler rabbitMessageHandler;

    public NnReceiver(NnRabbitMessageHandler rabbitMessageHandler) {
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

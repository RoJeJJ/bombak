package com.roje.bombak.nn.rabbit;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.rabbit.GameServiceReceiver;
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
public class NnReceiver extends GameServiceReceiver<NnPlayer, NnRoom> {

    public NnReceiver(Dispatcher dispatcher, RoomManager<NnPlayer, NnRoom> roomManager, MessageSender messageSender) {
        super(dispatcher, roomManager, messageSender);
    }

    @RabbitListener(queues = "nn-1")
    public void onMessage(byte[] data) {
        receiver(data);
    }
}

package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.processor.RoomProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/16
 **/
@Slf4j
@Component
@Message(id = NnConstant.Cmd.START_GAME_REQ)
public class StartGameProcessor implements RoomProcessor<NnPlayer, NnRoom> {

    @Override
    public void process(NnRoom room, NnPlayer p, byte[] data) throws Exception {
        if (p.uid() != room.ownerId()) {
            log.info("只能由房主开始游戏");
            return;
        }
        room.checkStart();
    }
}

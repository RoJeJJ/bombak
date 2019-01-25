package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.processor.RoomProcessor;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Component
@Message(id = Constant.Cmd.DISBAND_CARD_ROOM_REQ)
public class DisbandReqProcessor implements RoomProcessor<NnPlayer, NnRoom> {
    @Override
    public void process(NnRoom room, NnPlayer player, byte[] data) throws Exception {
        room.applyDisband(player);
    }
}

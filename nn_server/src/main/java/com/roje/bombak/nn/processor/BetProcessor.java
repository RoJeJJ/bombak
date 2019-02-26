package com.roje.bombak.nn.processor;

import com.google.protobuf.Any;
import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.NnMsg;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.processor.RoomProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/11
 **/
@Slf4j
@Component
@Message(id = NnConstant.Cmd.BET_REQ)
public class BetProcessor implements RoomProcessor<NnPlayer,NnRoom> {

    @Override
    public void process(NnRoom room, NnPlayer p, Any data) throws Exception {
        NnMsg.BetReq betReq = data.unpack(NnMsg.BetReq.class);
        room.bet(p,betReq.getBet());
    }
}

package com.roje.bombak.nn.processor;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.Nn;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.processor.RoomProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 抢庄
 *
 * @author pc
 * @version 1.0
 * @date 2019/1/11
 **/
@Slf4j
@Component
@Message(id = NnConstant.Cmd.RUSH_REQ)
public class RushProcessor implements RoomProcessor<NnPlayer,NnRoom> {

    @Override
    public void process(NnRoom room, NnPlayer p, byte[] data) throws Exception {
        if (room.isStartRush() && p.getRushFlag() == Nn.RushStatus.WaitRush) {
            Nn.RushReq rushReq = Nn.RushReq.parseFrom(data);
            int bet = rushReq.getMul();
            if (bet > room.config().rushMultiple) {
                log.warn("抢庄倍数设置错误:{}", bet);
                bet = room.config().rushMultiple;
            } else if (bet < 0) {
                log.warn("抢庄倍数设置错误:{}", bet);
                bet = 0;
            }
            room.rush(p, bet);
        }
    }
}

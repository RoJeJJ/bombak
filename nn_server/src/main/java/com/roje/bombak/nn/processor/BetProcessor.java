package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.Nn;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.processor.RoomProcessor;
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
    public void process(NnRoom room, NnPlayer p, byte[] data) throws Exception {
        if (room.isStartBet() && p.getBetFlag() == Nn.BetStatus.WaitBet) {
            Nn.BetReq betMsg = Nn.BetReq.parseFrom(data);
            int bet = betMsg.getBet();
            if (bet > room.config().betMultiple) {
                log.info("下注超过最大倍数{}/{}",bet,room.config().betMultiple);
                bet = room.config().betMultiple;
            } else if (bet < 1) {
                log.info("下注小于最小倍数{}/{}",bet,room.config().betMultiple);
                bet = 1;
            }
            room.bet(p,bet * room.config().baseScore);
        }
    }
}

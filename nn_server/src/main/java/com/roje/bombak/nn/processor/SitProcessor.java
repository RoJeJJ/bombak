package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.processor.RoomProcessor;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/10
 **/
@Slf4j
@Component
@Message(id = Constant.Cmd.SIT_DOWN_REQ)
public class SitProcessor implements RoomProcessor<NnPlayer, NnRoom> {

    private final RoomMessageSender sender;

    public SitProcessor(RoomMessageSender sender) {
        this.sender = sender;
    }

    @Override
    public void process(NnRoom room, NnPlayer p, byte[] data) throws Exception {
        if (room.roomType() == RoomMsg.RoomType.card && room.isCardRoundStart() && !room.config().joinHalfWay) {
            log.info("游戏已经开始不允许中途加入游戏");
            sender.sendError(p,Constant.Cmd.SIT_DOWN_REQ, NnConstant.ErrorCode.PROHIBIT_JOIN_HALF);
            return;
        }
        RoomMsg.SitDownReq sitReq = RoomMsg.SitDownReq.parseFrom(data);
        int seat = sitReq.getSeat();
        if (seat < 1 || seat > room.config().personNum) {
            log.info("座位号错误");
            return;
        }
        room.sitDown(p,seat);
    }
}

package com.roje.bombak.nn.processor;

import com.google.protobuf.Any;
import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.processor.RoomProcessor;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.room.Room;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/10
 **/
@Slf4j
@Component
@Message(id = RoomConstant.Cmd.SIT_DOWN_REQ)
public class SitProcessor implements RoomProcessor<NnPlayer, NnRoom> {

    private final RoomMessageSender sender;

    public SitProcessor(RoomMessageSender sender) {
        this.sender = sender;
    }

    @Override
    public void process(NnRoom room, NnPlayer p, Any any) throws Exception {
        if (room.getRoomType() == Room.CARD && room.isCardRoundStart() && !room.getSetting().joinHalfWay) {
            log.info("游戏已经开始不允许中途加入游戏");
            sender.sendErrMsgToGate(p, NnConstant.ErrorCode.PROHIBIT_JOIN_HALF);
            return;
        }
        RoomMsg.SitDownReq sitReq = any.unpack(RoomMsg.SitDownReq.class);
        room.sitDown(p,sitReq.getSeat());
    }
}

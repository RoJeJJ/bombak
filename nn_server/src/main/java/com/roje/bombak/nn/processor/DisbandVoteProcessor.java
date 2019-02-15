package com.roje.bombak.nn.processor;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.constant.Constant;
import com.roje.bombak.room.common.processor.RoomProcessor;
import com.roje.bombak.room.common.proto.RoomMsg;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Component
@Message(id = Constant.Cmd.DISBAND_CARD_ROOM_VOTE_REQ)
public class DisbandVoteProcessor implements RoomProcessor<NnPlayer, NnRoom> {
    @Override
    public void process(NnRoom room, NnPlayer player, byte[] data) throws Exception {
        RoomMsg.DisCardRoomVoteReq voteReq = RoomMsg.DisCardRoomVoteReq.parseFrom(data);
        room.disbandVote(player,voteReq.getVote());
    }
}
